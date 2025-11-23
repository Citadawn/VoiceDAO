package com.citadawn.speechapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.citadawn.speechapp.R;
import com.citadawn.speechapp.util.DialogHelper;
import com.citadawn.speechapp.util.EngineLabelHelper;
import com.citadawn.speechapp.util.LocaleHelper;
import com.citadawn.speechapp.util.StatusBarHelper;
import com.citadawn.speechapp.util.TtsEngineHelper;
import com.citadawn.speechapp.util.TtsLanguageVoiceHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * TTS 引擎和语言发音人信息浏览界面
 * 使用选项卡展示 TTS 引擎列表和语言发音人列表
 */
public class TtsBrowserActivity extends AppCompatActivity {

    // region 成员变量

    // 滚动位置保存
    private final Map<Integer, Integer> scrollPositions = new HashMap<>();
    private final Map<Integer, Integer> scrollOffsets = new HashMap<>();
    private TextToSpeech tts;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TtsBrowserPagerAdapter pagerAdapter;

    // endregion

    // region 生命周期

    /**
     * 启动 TTS 浏览器界面
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, TtsBrowserActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tts_browser);

        initViews();
        initTts();
        setupTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 在Android 15上，需要重新设置状态栏颜色
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            StatusBarHelper.forceStatusBarColor(getWindow());
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tts_browser, menu);
        return true;
    }

    // endregion

    // region 公开方法

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_tts_browser_info) {
            DialogHelper.showInfoDialog(this, R.string.tts_browser_info_title, R.string.tts_browser_info_content);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // endregion

    // region 私有方法

    /**
     * 初始化视图
     */
    private void initViews() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.tts_browser_title);
        }

        // 设置状态栏背景色和文字颜色
        StatusBarHelper.setupStatusBar(getWindow());

        // 处理系统窗口插入，避免与状态栏重叠
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化 ViewPager2 和 TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // 禁用ViewPager2的滑动切换，避免与ListView滚动冲突
        viewPager.setUserInputEnabled(false);

        // 创建适配器
        pagerAdapter = new TtsBrowserPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * 初始化 TTS
     */
    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TtsBrowser", "TTS 初始化成功");
                // TTS 初始化成功后刷新数据
                pagerAdapter.refreshData(tts);
            } else {
                Log.e("TtsBrowser", "TTS 初始化失败: " + status);
            }
        });
    }

    /**
     * 设置选项卡
     */
    private void setupTabs() {
        // 连接 TabLayout 和 ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_engines);
                    break;
                case 1:
                    tab.setText(R.string.tab_languages_voices);
                    break;
            }
        }).attach();

        // 添加页面变化监听器，保存滚动位置
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int lastPosition = -1;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 保存上一个页面的滚动位置
                if (lastPosition != -1 && pagerAdapter != null) {
                    pagerAdapter.saveCurrentScrollPosition(lastPosition);
                }

                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                // 当页面滚动状态改变时，确保所有ListView的滚动状态正确
                if (state == ViewPager2.SCROLL_STATE_IDLE && pagerAdapter != null) {
                    pagerAdapter.resetAllListViewScrollState();
                }
            }
        });
    }

    // endregion

    // region 内部类

    /**
     * ViewPager2 适配器
     */
    private static class TtsBrowserPagerAdapter
            extends androidx.recyclerview.widget.RecyclerView.Adapter<TtsBrowserPagerAdapter.ViewHolder> {

        private final TtsBrowserActivity activity;
        private final Map<Integer, BaseAdapter> adapters = new HashMap<>();
        private final Map<Integer, ListView> listViews = new HashMap<>();
        private TextToSpeech tts;

        public TtsBrowserPagerAdapter(TtsBrowserActivity activity) {
            this.activity = activity;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void refreshData(TextToSpeech tts) {
            this.tts = tts;
            // 清除缓存的适配器，强制重新创建
            adapters.clear();

            // 强制刷新所有数据，确保引擎列表正确显示
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 根据viewType选择不同的布局文件
            int layoutResId = (viewType == 1) ? R.layout.fragment_tts_browser_with_search
                    : R.layout.fragment_tts_browser;

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layoutResId, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            // 引擎列表使用viewType 0，语言发音人列表使用viewType 1
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (tts == null)
                return;

            ListView listView = holder.listView;
            listViews.put(position, listView);

            // 添加滚动监听器，实时保存滚动位置
            listView.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(android.widget.AbsListView view, int scrollState) {
                    // 滚动状态改变时保存位置
                    if (scrollState == android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                            saveScrollPosition(listView, currentPosition);
                        }

                        // 确保滚动状态正确重置
                        view.post(() -> {
                            view.setEnabled(true);
                            view.setFocusable(true);
                            view.setFocusableInTouchMode(true);
                        });
                    }
                }

                @Override
                public void onScroll(android.widget.AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    // 滚动时实时保存位置
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        saveScrollPosition(listView, currentPosition);
                    }
                }
            });

            // 如果是语言发音人页面，设置搜索功能
            if (position == 1) {
                setupSearchFunctionality(holder.itemView);
            }

            // 如果适配器已存在，直接使用；否则创建新的
            BaseAdapter adapter = adapters.get(position);
            if (adapter == null) {
                adapter = switch (position) {
                    case 0 -> createEngineAdapter();
                    case 1 -> createLanguageVoiceAdapter();
                    default -> null;
                };
                if (adapter != null) {
                    adapters.put(position, adapter);
                }
            }

            if (adapter != null) {
                listView.setAdapter(adapter);

                // 延迟恢复滚动位置，确保适配器完全加载
                listView.post(() -> restoreScrollPosition(listView, position));
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        private BaseAdapter createEngineAdapter() {
            // 使用工具类获取排序后的引擎列表
            List<TextToSpeech.EngineInfo> sortedEngines = TtsEngineHelper.getSortedEngines(tts);
            String defaultEngine = TtsEngineHelper.getDefaultEngineName(tts);

            return new EngineAdapter(activity, sortedEngines, defaultEngine);
        }

        private BaseAdapter createLanguageVoiceAdapter() {
            // 使用工具类获取语言和发音人
            Object[] result = TtsEngineHelper.getAvailableLanguagesAndVoices(tts);
            if (result != null) {
                @SuppressWarnings("unchecked")
                Set<Locale> languages = (Set<Locale>) result[0];
                @SuppressWarnings("unchecked")
                Set<Voice> voices = (Set<Voice>) result[1];
                return new LanguageVoiceAdapter(activity, languages, voices, tts);
            }
            return null;
        }

        /**
         * 保存当前ListView的滚动位置
         */
        private void saveScrollPosition(ListView listView, int position) {
            if (listView != null) {
                int firstVisiblePosition = listView.getFirstVisiblePosition();
                View firstVisibleView = listView.getChildAt(0);
                int top = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

                // 保存位置和偏移量
                activity.scrollPositions.put(position, firstVisiblePosition);
                activity.scrollOffsets.put(position, top);
            }
        }

        /**
         * 恢复ListView的滚动位置
         */
        private void restoreScrollPosition(ListView listView, int position) {
            if (listView != null) {
                Integer scrollPosition = activity.scrollPositions.get(position);
                Integer scrollOffset = activity.scrollOffsets.get(position);

                if (scrollPosition != null) {
                    // 使用更精确的恢复方法
                    listView.post(() -> {
                        // 确保ListView状态正确
                        listView.setEnabled(true);
                        listView.setFocusable(true);
                        listView.setFocusableInTouchMode(true);

                        if (scrollOffset != null) {
                            // 恢复精确位置和偏移量
                            listView.setSelectionFromTop(scrollPosition, scrollOffset);
                        } else {
                            // 备用方法
                            listView.setSelection(scrollPosition);
                        }
                    });
                }
            }
        }

        /**
         * 获取当前可见的ListView，用于保存滚动位置
         */
        public void saveCurrentScrollPosition(int currentPosition) {
            ListView listView = listViews.get(currentPosition);
            if (listView != null) {
                saveScrollPosition(listView, currentPosition);
            }
        }

        /**
         * 重置所有ListView的滚动状态
         */
        public void resetAllListViewScrollState() {
            for (ListView listView : listViews.values()) {
                if (listView != null) {
                    // 强制重置滚动状态
                    listView.clearFocus();
                    listView.requestFocus();

                    // 确保滚动功能正常
                    listView.post(() -> {
                        listView.setEnabled(true);
                        listView.setFocusable(true);
                        listView.setFocusableInTouchMode(true);
                    });
                }
            }
        }

        /**
         * 设置搜索功能
         */
        private void setupSearchFunctionality(View view) {
            EditText searchEditText = view.findViewById(R.id.search_edit_text);
            ImageButton clearSearchButton = view.findViewById(R.id.clear_search_button);

            if (searchEditText == null || clearSearchButton == null)
                return;

            // 搜索文本变化监听
            searchEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    String query = s.toString().trim();

                    // 显示/隐藏清除按钮
                    clearSearchButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                    // 执行搜索
                    performSearch(query);
                }
            });

            // 清除搜索按钮点击监听
            clearSearchButton.setOnClickListener(v -> {
                searchEditText.setText("");
                searchEditText.requestFocus();
            });

            // 搜索框焦点变化监听
            searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !searchEditText.getText().toString().isEmpty()) {
                    clearSearchButton.setVisibility(View.VISIBLE);
                }
            });
        }

        /**
         * 执行搜索
         */
        private void performSearch(String query) {
            ListView targetListView = listViews.get(1);
            if (targetListView != null) {
                BaseAdapter adapter = (BaseAdapter) targetListView.getAdapter();
                if (adapter instanceof LanguageVoiceAdapter languageVoiceAdapter) {
                    languageVoiceAdapter.filter(query);
                }
            }
        }

        static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            final ListView listView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                listView = itemView.findViewById(R.id.list_view);
            }
        }
    }

    /**
     * TTS 引擎列表适配器
     */
    private static class EngineAdapter extends BaseAdapter {

        private final Context context;
        private final List<TextToSpeech.EngineInfo> engines;
        private final LayoutInflater inflater;
        private final String defaultEngineName;

        public EngineAdapter(Context context, List<TextToSpeech.EngineInfo> engines, String defaultEngineName) {
            this.context = context;
            this.engines = engines;
            this.inflater = LayoutInflater.from(context);
            this.defaultEngineName = defaultEngineName;
        }

        @Override
        public int getCount() {
            return engines.size();
        }

        @Override
        public TextToSpeech.EngineInfo getItem(int position) {
            return engines.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_engine, parent, false);
                holder = new ViewHolder();
                holder.iconView = convertView.findViewById(R.id.engine_icon);
                holder.nameView = convertView.findViewById(R.id.engine_name);
                holder.packageView = convertView.findViewById(R.id.engine_package);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TextToSpeech.EngineInfo engine = getItem(position);

            // 设置图标
            try {
                if (engine.icon != 0) {
                    Drawable icon = context.getPackageManager().getDrawable(engine.name, engine.icon, null);
                    holder.iconView.setImageDrawable(icon);
                } else {
                    holder.iconView.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } catch (Exception e) {
                Log.w("EngineAdapter", "无法加载引擎图标: " + engine.name, e);
                holder.iconView.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // 设置名称和包名，默认引擎添加标识
            // 使用官方资源按“应用界面语言”获取引擎本地化名称
            String engineName = EngineLabelHelper.getLocalizedAppLabel(context, engine.name);
            if (engineName.isEmpty()) {
                engineName = engine.name;
            }
            if (engine.name.equals(defaultEngineName)) {
                // 智能添加默认标识，避免重复的圆括号
                String defaultLabel = context.getString(R.string.default_value);
                if (engineName.contains("(") && engineName.contains(")")) {
                    // 如果引擎名称已经包含圆括号，替换圆括号内容为默认标识
                    int firstParen = engineName.indexOf("(");
                    int lastParen = engineName.lastIndexOf(")");
                    if (firstParen >= 0 && lastParen > firstParen) {
                        String baseName = engineName.substring(0, firstParen).trim();
                        // defaultLabel本身就带圆括号，直接拼接
                        engineName = baseName + " " + defaultLabel;
                    } else {
                        engineName += " " + defaultLabel;
                    }
                } else {
                    // 如果没有圆括号，直接添加defaultLabel（已包含圆括号）
                    engineName += " " + defaultLabel;
                }
            }
            holder.nameView.setText(engineName);
            holder.packageView.setText(engine.name);

            return convertView;
        }

        private static class ViewHolder {
            ImageView iconView;
            TextView nameView;
            TextView packageView;
        }
    }

    /**
     * 语言和发音人列表适配器
     */
    private static class LanguageVoiceAdapter extends BaseAdapter implements android.widget.SectionIndexer {

        private final Context context;
        private final List<LanguageVoiceItem> allItems; // 所有原始数据
        private final List<LanguageVoiceItem> items; // 当前显示的数据
        private final LayoutInflater inflater;
        private final TextToSpeech tts;
        private String currentFilter = ""; // 当前搜索关键词

        // 快速滚动索引相关
        private String[] sections;
        private Integer[] sectionPositions;

        public LanguageVoiceAdapter(Context context, Set<Locale> languages, Set<Voice> voices, TextToSpeech tts) {
            this.context = context;
            this.tts = tts;
            this.inflater = LayoutInflater.from(context);

            // 使用改进的方法构建语言和发音人数据
            this.allItems = buildLanguageVoiceItems(languages, voices);
            this.items = new ArrayList<>(this.allItems); // 初始显示所有数据

            // 初始化快速滚动索引
            buildSectionIndex();
        }

        /**
         * 构建语言和发音人数据项
         * 使用工具类改进版本：智能选择默认发音人，提供更好的用户体验
         */
        private List<LanguageVoiceItem> buildLanguageVoiceItems(Set<Locale> languages, Set<Voice> voices) {
            List<LanguageVoiceItem> items = new ArrayList<>();

            // 使用工具类获取默认语言
            Locale defaultLocale = TtsEngineHelper.getDefaultLanguage(tts);

            // 使用工具类构建语言和发音人的映射关系
            Map<Locale, List<Voice>> languageVoicesMap = TtsLanguageVoiceHelper.buildLanguageVoicesMap(languages,
                    voices);

            // 使用工具类确定每个语言的默认发音人
            Map<Locale, Voice> languageDefaultVoices = TtsLanguageVoiceHelper
                    .determineLanguageDefaultVoices(languageVoicesMap, null);

            // 使用工具类按语言名称排序，默认语言排在最前面
            List<Locale> sortedLocales;
            if (defaultLocale != null) {
                sortedLocales = TtsLanguageVoiceHelper.sortLocalesByDisplayName(languages, defaultLocale, context);
            } else {
                sortedLocales = TtsLanguageVoiceHelper.sortLocalesByDisplayName(languages, context);
            }

            // 构建最终列表：语言项 + 该语言下的发音人
            for (Locale locale : sortedLocales) {
                List<Voice> voiceList = languageVoicesMap.get(locale);
                Voice defaultVoice = languageDefaultVoices.get(locale);

                // 判断是否为默认语言
                boolean isDefaultLanguage = locale.equals(defaultLocale);

                // 先添加语言项，标识是否为默认语言
                items.add(new LanguageVoiceItem(locale, null, defaultVoice, isDefaultLanguage));

                // 再添加该语言下的发音人，默认发音人排在前面
                if (defaultVoice != null) {
                    // 使用工具类排序发音人
                    List<Voice> sortedVoices = TtsLanguageVoiceHelper.sortVoicesByDefault(voiceList, defaultVoice);

                    // 添加所有发音人
                    for (Voice voice : sortedVoices) {
                        items.add(new LanguageVoiceItem(locale, voice, defaultVoice));
                    }
                } else {
                    // 如果没有默认发音人，按原顺序添加
                    if (voiceList != null) {
                        for (Voice voice : voiceList) {
                            items.add(new LanguageVoiceItem(locale, voice, null));
                        }
                    }
                }
            }

            return items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public LanguageVoiceItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_language_voice, parent, false);
                holder = new ViewHolder();
                holder.languageView = convertView.findViewById(R.id.language_name);
                holder.tagView = convertView.findViewById(R.id.language_tag);
                holder.supportView = convertView.findViewById(R.id.language_support);
                holder.voiceView = convertView.findViewById(R.id.voice_info);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LanguageVoiceItem item = getItem(position);

            // 设置语言信息，使用当前界面语言获取本地化名称
            String languageName = item.locale.getDisplayName(LocaleHelper.getCurrentLocale(context));

            // 如果是默认语言，添加默认标识
            if (item.voice == null && item.isDefaultLanguage) {
                String defaultLabel = context.getString(R.string.default_value);
                languageName += " " + defaultLabel;
            }

            holder.languageView.setText(languageName);
            holder.tagView.setText(item.locale.toLanguageTag());

            // 设置支持情况
            int supportStatus = tts.isLanguageAvailable(item.locale);
            String supportText;
            int supportColorRes = switch (supportStatus) {
                case TextToSpeech.LANG_AVAILABLE -> {
                    supportText = context.getString(R.string.language_available);
                    yield R.color.tts_support_full;
                }
                case TextToSpeech.LANG_COUNTRY_AVAILABLE -> {
                    supportText = context.getString(R.string.language_country_available);
                    yield R.color.tts_support_partial;
                }
                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                    supportText = context.getString(R.string.language_country_available); // 使用国家支持的文本，因为没有单独的变体支持文本
                    yield R.color.tts_support_variant;
                }
                case TextToSpeech.LANG_MISSING_DATA -> {
                    supportText = context.getString(R.string.language_missing_data);
                    yield R.color.tts_support_missing_data;
                }
                case TextToSpeech.LANG_NOT_SUPPORTED -> {
                    supportText = context.getString(R.string.language_not_supported);
                    yield R.color.tts_support_none;
                }
                default -> {
                    supportText = context.getString(R.string.language_unknown);
                    yield R.color.tts_support_none;
                }
            };
            holder.supportView.setText(supportText);
            holder.supportView.setBackgroundColor(ContextCompat.getColor(context, supportColorRes));

            // 设置发音人信息
            if (item.voice != null) {
                // 这是发音人项，调整样式
                holder.languageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.sp_14));
                holder.languageView.setTextColor(ContextCompat.getColor(context, R.color.gray_666));
                holder.languageView.setPadding((int) context.getResources().getDimension(R.dimen.dp_32), 0, 0, 0); // 缩进

                // 检查是否为默认发音人
                boolean isDefault = item.voice.equals(item.defaultVoice);
                // 使用工具类清理发音人名称，去除技术标识符
                String voiceName = TtsLanguageVoiceHelper.cleanVoiceName(item.voice.getName());
                if (isDefault) {
                    // default_value本身就包含圆括号，直接拼接
                    voiceName += " " + context.getString(R.string.default_value);
                }

                StringBuilder voiceInfo = new StringBuilder();
                voiceInfo.append("• ").append(context.getString(R.string.voice_name)).append(": ").append(voiceName);

                // 添加特性信息（如果存在且有意义）
                Set<String> features = item.voice.getFeatures();
                if (TtsLanguageVoiceHelper.shouldShowFeatures(features)) {
                    String featuresText = " [" + String.join(", ", features) + "]";
                    voiceInfo.append(featuresText);
                }

                voiceInfo.append("\n");
                voiceInfo.append("• ").append(context.getString(R.string.voice_network)).append(": ")
                        .append(item.voice.isNetworkConnectionRequired() ? context.getString(R.string.yes)
                                : context.getString(R.string.no));
                voiceInfo.append("\n");
                voiceInfo.append("• ").append(context.getString(R.string.voice_quality)).append(": ")
                        .append(getQualityText(item.voice.getQuality()));
                voiceInfo.append("\n");
                voiceInfo.append("• ").append(context.getString(R.string.voice_latency)).append(": ")
                        .append(getLatencyText(item.voice.getLatency()));

                holder.voiceView.setText(voiceInfo.toString());
                holder.voiceView.setVisibility(View.VISIBLE);
            } else {
                // 这是语言项，使用默认样式
                holder.languageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.sp_16));
                holder.languageView.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                holder.languageView.setPadding(0, 0, 0, 0);
                holder.voiceView.setVisibility(View.GONE);
            }

            return convertView;
        }

        private String getQualityText(int quality) {
            return switch (quality) {
                case Voice.QUALITY_VERY_LOW -> context.getString(R.string.quality_very_low);
                case Voice.QUALITY_LOW -> context.getString(R.string.quality_low);
                case Voice.QUALITY_NORMAL -> context.getString(R.string.quality_normal);
                case Voice.QUALITY_HIGH -> context.getString(R.string.quality_high);
                case Voice.QUALITY_VERY_HIGH -> context.getString(R.string.quality_very_high);
                default -> context.getString(R.string.quality_unknown);
            };
        }

        private String getLatencyText(int latency) {
            return switch (latency) {
                case Voice.LATENCY_VERY_LOW -> context.getString(R.string.latency_very_low);
                case Voice.LATENCY_LOW -> context.getString(R.string.latency_low);
                case Voice.LATENCY_NORMAL -> context.getString(R.string.latency_normal);
                case Voice.LATENCY_HIGH -> context.getString(R.string.latency_high);
                case Voice.LATENCY_VERY_HIGH -> context.getString(R.string.latency_very_high);
                default -> context.getString(R.string.latency_unknown);
            };
        }

        /**
         * 过滤语言列表
         *
         * @param query 搜索关键词
         */
        public void filter(String query) {
            String newFilter = query.toLowerCase().trim();

            // 如果过滤条件没有变化，不需要重新过滤
            if (currentFilter.equals(newFilter)) {
                return;
            }

            currentFilter = newFilter;
            int oldSize = items.size();
            items.clear();

            if (currentFilter.isEmpty()) {
                // 如果搜索为空，显示所有数据
                items.addAll(allItems);
            } else {
                // 过滤匹配的语言
                for (LanguageVoiceItem item : allItems) {
                    String languageName = item.locale.getDisplayName(LocaleHelper.getCurrentLocale(context))
                            .toLowerCase();
                    String languageTag = item.locale.toString().toLowerCase();

                    // 如果搜索关键词匹配语言名称或语言标签，则包含该项
                    if (languageName.contains(currentFilter) || languageTag.contains(currentFilter)) {
                        items.add(item);
                    }
                }
            }

            // 对于BaseAdapter，使用notifyDataSetChanged，但可以优化调用时机
            // 只有在数据真正发生变化时才调用
            if (oldSize != items.size()) {
                // 重新构建索引
                buildSectionIndex();
                notifyDataSetChanged();
            }
        }

        /**
         * 构建快速滚动索引
         */
        private void buildSectionIndex() {
            List<String> sectionList = new ArrayList<>();
            List<Integer> positionList = new ArrayList<>();

            String previousSection = "";
            for (int i = 0; i < items.size(); i++) {
                LanguageVoiceItem item = items.get(i);
                if (item.voice == null) { // 只为语言项创建索引，跳过发音人项
                    String languageName = item.locale.getDisplayName(LocaleHelper.getCurrentLocale(context));
                    if (!languageName.isEmpty()) {
                        String section = languageName.substring(0, 1).toUpperCase();
                        if (!section.equals(previousSection)) {
                            sectionList.add(section);
                            positionList.add(i);
                            previousSection = section;
                        }
                    }
                }
            }

            sections = sectionList.toArray(new String[0]);
            sectionPositions = positionList.toArray(new Integer[0]);
        }

        // SectionIndexer接口实现
        @Override
        public Object[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            if (sections == null || sectionIndex < 0 || sectionIndex >= sections.length) {
                return 0;
            }
            return sectionPositions[sectionIndex];
        }

        @Override
        public int getSectionForPosition(int position) {
            if (sections == null || position < 0 || position >= getCount()) {
                return 0;
            }

            for (int i = sectionPositions.length - 1; i >= 0; i--) {
                if (position >= sectionPositions[i]) {
                    return i;
                }
            }
            return 0;
        }

        private static class ViewHolder {
            TextView languageView;
            TextView tagView;
            TextView supportView;
            TextView voiceView;
        }

        private static class LanguageVoiceItem {
            final Locale locale;
            final Voice voice;
            final Voice defaultVoice; // 该语言的默认发音人
            final boolean isDefaultLanguage; // 是否为系统默认语言

            public LanguageVoiceItem(Locale locale, Voice voice, Voice defaultVoice) {
                this(locale, voice, defaultVoice, false);
            }

            public LanguageVoiceItem(Locale locale, Voice voice, Voice defaultVoice, boolean isDefaultLanguage) {
                this.locale = locale;
                this.voice = voice;
                this.defaultVoice = defaultVoice;
                this.isDefaultLanguage = isDefaultLanguage;
            }
        }
    }

    // endregion
}