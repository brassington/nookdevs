/*
/*
 * Copyright 2010 nookDevs
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nookdevs.library;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.*;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.bravo.ecm.service.ScannedFile;
import com.nookdevs.common.CustomGallery;
import com.nookdevs.common.IconArrayAdapter;
import com.nookdevs.common.ImageAdapter;
import com.nookdevs.common.nookBaseActivity;

public class NookLibrary extends nookBaseActivity implements OnItemClickListener, OnLongClickListener, OnClickListener {
    private List<ScannedFile> m_Files = new ArrayList<ScannedFile>(200);
    public static final int MAX_FILES_PER_BATCH = 99999;
    private boolean m_SearchView = false;

    private Button backButton, upButton, downButton;
    private ImageButton goButton;
    protected static final int VIEW_DETAILS = 0;
    protected static final int SORT_BY = 1;
    protected static final int SEARCH = 2;
    protected static final int SHOW_COVERS = 3;
    protected static final int SHOW = 4;
    protected static final int AUTHORS = 5;
    protected static final int REFRESH = 6;
    protected static final int VIEW_BY = 7;
    protected static final int SHOW_ARCHIVED = 8;
    protected static final int PAGE_NUMBERS = 9;
    protected static final int SCREENSAVER = 10;
    protected static final int HELP = 11;
    protected static final int CLOSE = 12;
    protected static final int LOCAL_BOOKS = 0;
    protected static final int BN_BOOKS = 1;
    protected static final int ALL_BOOKS = -1;
    protected static final int ADD_LIB_MENU = 10;
    protected int m_AddLibIndex = 2;
    protected static final int FICTIONWISE_BOOKS = 0;
    protected static final int SMASHWORDS = 1;
    private ConditionVariable m_LocalScanDone = new ConditionVariable();
    private ConditionVariable m_BNBooksLock = new ConditionVariable();
    private ConditionVariable m_FictionwiseLock = new ConditionVariable();
    private ConditionVariable m_SmashwordsLock = new ConditionVariable();
    private static final int WEB_SCROLL_PX = 750;
    private Toast m_Toast = null;
    private boolean m_ArchiveView = false;
    public static final String PREF_FILE = "NookLibrary";
    public static final int SORT_REVERSE_ORDER=4;
    private boolean m_Reversed=false;

    private int[] icons =
        {
            -1, R.drawable.submenu_image, R.drawable.search_image, R.drawable.covers_image, R.drawable.submenu_image,
            R.drawable.submenu_image, R.drawable.submenu_image, R.drawable.submenu_image, -1, -1,
            R.drawable.submenu_image, -1, -1
        };

    private int[] subicons = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };
    private int[] viewicons = {
        -1, -1, -1, -1
    };
    private int[] refreshicons = {
        -1, -1, -1
    };
    private int[] screensavericons = {
        R.drawable.submenu_image, -1, -1, -1
    };
    private ListView lview;
    private ListView submenu;
    private ViewAnimator animator;
    private int m_SubMenuType = -1;
    private PageViewHelper pageViewHelper;
    private Handler m_Handler = new Handler();
    private ImageAdapter m_IconAdapter = null;
    private CustomGallery m_IconGallery = null;
    private Button m_CloseBtn = null;
    private Button m_Archive = null;
    IconArrayAdapter<CharSequence> m_ListAdapter = null;
    IconArrayAdapter<CharSequence> m_SortAdapter = null;
    IconArrayAdapter<CharSequence> m_ViewAdapter = null;
    IconArrayAdapter<CharSequence> m_RefreshAdapter = null;
    IconArrayAdapter<CharSequence> m_ScreenSaverAdapter = null;
    IconArrayAdapter<CharSequence> m_LibsAdapter = null;
    ArrayAdapter<CharSequence> m_ShowAdapter = null;
    ArrayAdapter<String> m_AuthorAdapter = null;
    List<CharSequence> m_SortMenuValues = null;
    List<CharSequence> m_ViewMenuValues = null;
    List<CharSequence> m_ScreenSaverMenuValues = null;
    ConnectivityManager.WakeLock m_Lock;
    protected List<CharSequence> m_ShowValues = null;
    protected List<String> m_AuthorValues = null;
    int m_ShowIndex = 0;
    int m_AuthorIndex = 0;
    int m_ScreenSaverImageCount = 0;
    ImageButton m_CoverBtn = null;
    TextView m_Details = null;
    ScrollView m_DetailsScroll = null;
    ViewAnimator m_PageViewAnimator = null;
    TextView m_DetailsPage = null;
    private boolean m_ScanInProgress = false;
    private OtherBooks m_OtherBooks;
    private BNBooks m_BNBooks;
    private FictionwiseBooks m_FictionwiseBooks = null;
    private Smashwords m_Smashwords = null;
    private int m_View=PageViewHelper.BOOKS;
    private int m_Level=0;
    private TextView m_GalleryTitle;
    private Intent m_PrevIntent = null;
    private static final String PREFS_KEY_SCREENSAVER_FOLDER_NAME = "SCREENSAVER_FOLDER_NAME";
    private static final String PREFS_KEY_SCREENSAVER_IMAGE_COUNT = "SCREENSAVER_IMAGE_COUNT";
    private boolean m_ShowPageNumbers=true;
    private boolean m_PageNumbersUpdated=false;

    public Handler getHandler() {
        return m_Handler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGTAG = "nookLibrary";
        NAME = getString(R.string.caption);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ScannedFile.setContext(this);
        goButton = (ImageButton) findViewById(R.id.go);
        backButton = (Button) findViewById(R.id.back);
        upButton = (Button) findViewById(R.id.up);
        downButton = (Button) findViewById(R.id.down);
        m_Archive = (Button) findViewById(R.id.archive);
        m_Archive.setOnLongClickListener(new OnLongClickListener() {

            public boolean onLongClick(View arg0) {
                // confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(NookLibrary.this);
                builder.setTitle(R.string.delete);
                builder.setMessage(R.string.confirm);
                builder.setNegativeButton(android.R.string.no, null).setCancelable(true);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success=false;
                        ScannedFile file = pageViewHelper.getCurrent();
                        // delete book
                        m_Files.remove(file);
                        if (file.matchSubject(getString(R.string.fictionwise))) {
                            success=m_FictionwiseBooks.deleteBook(file);
                        } else if (file.matchSubject("B&N")) {
                            success=m_BNBooks.deleteBook(file);
                        } else if (file.matchSubject(getString(R.string.smashwords))) {
                            success=m_Smashwords.deleteBook(file);
                        } else {
                            // local
                            success=m_OtherBooks.deleteBook(file);
                        }
                        if( success)
                            m_Files.remove(file);
                        else {
                            displayAlert(R.string.archive_error);
                            return;
                        }
                        // Fix for Issue 89
                        List<ScannedFile> f = pageViewHelper.getFiles();
                        f.remove(file);
                        pageViewHelper.setFiles(f);
                        List<String> tmpList = getAvailableKeywords();
                        Comparator<String> c = new Comparator<String>() {
                            public int compare(String object1, String object2) {
                                if (object1 != null) {
                                    return object1.compareToIgnoreCase(object2);
                                } else {
                                    return 1;
                                }
                            }
                        };
                        Collections.sort(tmpList, c);
                        m_ShowValues = new ArrayList<CharSequence>(tmpList.size() + 1);
                        m_ShowValues.add(getString(R.string.all));
                        if (ScannedFile.m_StandardKeywords != null) {
                            m_ShowValues.addAll(ScannedFile.m_StandardKeywords);
                        }
                        m_ShowValues.addAll(tmpList);
                        m_ShowAdapter =
                            new ArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem2, m_ShowValues);
                        backButton.performClick();
                    }
                });
                builder.show();
                return true;
            }

        });
        m_Archive.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                ScannedFile file = pageViewHelper.getCurrent();
                boolean success=false;
                if (file.getStatus() == null || !(file.getStatus().equals(BNBooks.ARCHIVED) ||
                    file.getStatus().equals(BNBooks.ARCHIVED + " " + BNBooks.SAMPLE))) {
                    // archive book
                    if (file.matchSubject(getString(R.string.fictionwise))) {
                        success = m_FictionwiseBooks.archiveBook(file, true);
                    } else if (file.matchSubject("B&N")) {
                        success=m_BNBooks.archiveBook(file, true);
                    } else if (file.matchSubject(getString(R.string.smashwords))) {
                        success=m_Smashwords.archiveBook(file, true);
                    } else {
                        // local
                        success=m_OtherBooks.archiveBook(file, true);
                    }
                    if( success) {
                        m_Files.remove(file);
                    }
                    else {
                        displayAlert(R.string.archive_error);
                    }
                } else {
                    if (file.matchSubject(getString(R.string.fictionwise))) {
                        success=m_FictionwiseBooks.archiveBook(file, false);
                    } else if (file.matchSubject("B&N")) {
                        // unarchive B&N book
                        success=m_BNBooks.archiveBook(file, false);

                    } else if (file.matchSubject(getString(R.string.smashwords))) {
                        success=m_Smashwords.archiveBook(file, false);
                    } else {
                        success=m_OtherBooks.archiveBook(file, false);
                    }
                    if( success)
                        m_Files.remove(file);
                    else {
                        displayAlert(R.string.archive_error);
                    }
                }
                // Fix for Issue 89
                List<ScannedFile> f = pageViewHelper.getFiles();
                f.remove(file);
                pageViewHelper.setFiles(f);
                List<String> tmpList = getAvailableKeywords();
                Comparator<String> c = new Comparator<String>() {
                    public int compare(String object1, String object2) {
                        if (object1 != null) {
                            return object1.compareToIgnoreCase(object2);
                        } else {
                            return 1;
                        }
                    }
                };
                Collections.sort(tmpList, c);
                m_ShowValues = new ArrayList<CharSequence>(tmpList.size() + 1);
                m_ShowValues.add(getString(R.string.all));
                if (ScannedFile.m_StandardKeywords != null) {
                    m_ShowValues.addAll(ScannedFile.m_StandardKeywords);
                }
                m_ShowValues.addAll(tmpList);
                m_ShowAdapter = new ArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem2, m_ShowValues);
                backButton.performClick();
            }
        });
        lview = (ListView) findViewById(R.id.list);
        submenu = (ListView) findViewById(R.id.sublist);
        animator = (ViewAnimator) findViewById(R.id.listviewanim);
        submenu.setOnItemClickListener(this);
        submenu.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (m_SubMenuType == REFRESH && arg2 >= 2 && arg2 != m_AddLibIndex) {
                    backButton.performClick();
                    CharSequence name = m_RefreshAdapter.getItem(arg2);
                    if (name.equals(getString(R.string.fictionwise))) {
                        m_FictionwiseBooks.deleteAll();
                        m_LibsAdapter.insert(name, 0);
                    } else if (name.equals(getString(R.string.smashwords))) {
                        m_Smashwords.deleteAll();
                        m_LibsAdapter.insert(name, 1);
                    }
                    m_RefreshAdapter.remove(name);
                    m_AddLibIndex--;
                    queryFolders(LOCAL_BOOKS);
                    return true;
                }
                return false;
            }

        });
        CharSequence[] menuitems = getResources().getTextArray(R.array.mainmenu);
        List<CharSequence> menuitemsList = Arrays.asList(menuitems);
        m_ListAdapter = new IconArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem, menuitemsList, icons);
        m_ListAdapter.setImageField(R.id.ListImageView);
        m_ListAdapter.setTextField(R.id.ListTextView);
        m_ListAdapter.setSubTextField(R.id.ListSubTextView);
        menuitems = getResources().getTextArray(R.array.sortmenu);
        m_SortMenuValues = Arrays.asList(menuitems);
        m_SortAdapter =
            new IconArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem, m_SortMenuValues, subicons);
        m_SortAdapter.setImageField(R.id.ListImageView);
        m_SortAdapter.setTextField(R.id.ListTextView);
        menuitems = getResources().getTextArray(R.array.refreshmenu);
        m_FictionwiseBooks = new FictionwiseBooks(this);
        m_Smashwords = new Smashwords(this);
        ArrayList<CharSequence> list = new ArrayList<CharSequence>(5);
        list.addAll(Arrays.asList(menuitems));
        list.addAll(getAdditionalLibraries());
        m_RefreshAdapter =
            new IconArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem, list, refreshicons);
        m_RefreshAdapter.setImageField(R.id.ListImageView);
        m_RefreshAdapter.setTextField(R.id.ListTextView);
        menuitems = getResources().getTextArray(R.array.viewmenu);
        m_ViewMenuValues = Arrays.asList(menuitems);
        m_ViewAdapter = new IconArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem, m_ViewMenuValues, viewicons);
        m_ViewAdapter.setImageField(R.id.ListImageView);
        m_ViewAdapter.setTextField(R.id.ListTextView);
        menuitems = getResources().getTextArray(R.array.screensavermenu);
        m_ScreenSaverMenuValues = Arrays.asList(menuitems);
        m_ScreenSaverAdapter =
            new IconArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem, m_ScreenSaverMenuValues, screensavericons);
        m_ScreenSaverAdapter.setImageField(R.id.ListImageView);
        m_ScreenSaverAdapter.setTextField(R.id.ListTextView);
        m_ScreenSaverAdapter.setSubTextField(R.id.ListSubTextView);
        updateScreenSaverMenuItem();
        WebView web = new WebView(this);
        lview.setAdapter(m_ListAdapter);
        lview.setOnItemClickListener(this);
        goButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        upButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        upButton.setOnLongClickListener(this);
        downButton.setOnLongClickListener(this);
        m_IconGallery = ((CustomGallery) findViewById(R.id.icongallery));
        TypedArray a = this.obtainStyledAttributes(R.styleable.default_gallery);
        int backid = a.getResourceId(R.styleable.default_gallery_android_galleryItemBackground, 0);
        a.recycle();
        m_IconGallery.setVisibility(View.INVISIBLE);
        GalleryClickListener galleryListener = new GalleryClickListener();
        m_IconGallery.setOnItemClickListener(galleryListener);
        m_IconGallery.setAlwaysDrawnWithCacheEnabled(true);
        m_IconGallery.setCallbackDuringFling(false);
        m_GalleryTitle = (TextView) findViewById(R.id.gallery_title);
        m_GalleryTitle.setVisibility(View.INVISIBLE);
        m_CloseBtn = (Button) (findViewById(R.id.closeButton));
        m_CloseBtn.setVisibility(View.INVISIBLE);
        m_CloseBtn.setOnClickListener(this);
        m_IconAdapter = new ImageAdapter(this, null, null);
        m_IconAdapter.setBackgroundStyle(backid);
        m_IconAdapter.setDefault(R.drawable.no_cover);
        m_IconGallery.setOnItemSelectedListener(galleryListener);
        m_CoverBtn = (ImageButton) findViewById(R.id.cover);
        m_Details = (TextView) findViewById(R.id.details);
        m_DetailsScroll = (ScrollView) findViewById(R.id.detailsscroll);
        m_CoverBtn.setVisibility(View.INVISIBLE);
        m_CoverBtn.setOnClickListener(this);
        m_Details.setVisibility(View.INVISIBLE);
        m_DetailsScroll.setVisibility(View.INVISIBLE);
        m_PageViewAnimator = (ViewAnimator) findViewById(R.id.pageview);
        m_DetailsPage = (TextView) findViewById(R.id.pageview2);
        m_OtherBooks = new OtherBooks(this);
        m_BNBooks = new BNBooks(this);
        m_Archive.setVisibility(View.INVISIBLE);
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        m_ShowPageNumbers = prefs.getBoolean("PAGE_NUMBERS", true);
        m_ListAdapter.setSubText(PAGE_NUMBERS, m_ShowPageNumbers?getString(R.string.on):getString(R.string.off));
        ConnectivityManager cmgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        m_Lock = cmgr.newWakeLock(1, "NookLibrary.scanTask" + hashCode());
        queryFolders();
    }

    private List<CharSequence> getAdditionalLibraries() {
        ArrayList<CharSequence> list = new ArrayList<CharSequence>(2);
        String fictionwise = getString(R.string.fictionwise);
        String smashwords = getString(R.string.smashwords);
        if (m_FictionwiseBooks.getUser()) {
            list.add(fictionwise);
        }
        if (m_Smashwords.getUser()) {
            list.add(smashwords);
        }
        list.add(getString(R.string.add_lib));
        m_AddLibIndex = list.size() + 1;
        ArrayList<CharSequence> libsList = new ArrayList<CharSequence>(2);
        if (!list.contains(fictionwise)) {
            libsList.add(fictionwise);
        }
        if (!list.contains(smashwords)) {
            libsList.add(smashwords);
        }
        m_LibsAdapter = new IconArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem, libsList, subicons);
        m_LibsAdapter.setImageField(R.id.ListImageView);
        m_LibsAdapter.setTextField(R.id.ListTextView);
        return list;

    }

    private List<ScannedFile> searchFiles(String keyword, List<ScannedFile> list) {
        List<ScannedFile> results = new ArrayList<ScannedFile>(m_Files.size());
        String key = keyword.toLowerCase();
        for (ScannedFile file : list) {
            if (file.getData().contains(key)) {
                results.add(file);
            }
        }
        Vector<String> eans = m_BNBooks.searchDescription(keyword);
        eans.addAll(m_FictionwiseBooks.searchDescription(keyword));
        eans.addAll(m_Smashwords.searchDescription(keyword));
        Vector<String> paths = m_OtherBooks.searchDescription(keyword);
        for (ScannedFile file : list) {
            if (!results.contains(file) && ( eans.contains(file.getEan())) || paths.contains(file.getPathName())) {
                results.add(file);
            } else {
                String keyStr = getKeywords(file);
                if( keyStr != null && keyStr.contains(keyword)) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    private List<ScannedFile> filterFiles(String keyword, List<ScannedFile> list) {
        List<ScannedFile> results = new ArrayList<ScannedFile>(m_Files.size());
        for (ScannedFile file : list) {
            if (file.matchSubject(keyword)) {
                results.add(file);
            } else {
                String k = getKeywords(file);
                if( k != null && k.contains(keyword)) {
                    results.add(file);
                }
            }
        }
        return results;
    }

    @Override
    public void readSettings() {
        int sortType = ScannedFile.SORT_BY_NAME;
        try {
            sortType = getSharedPreferences(PREF_FILE, MODE_PRIVATE).getInt("SORT_BY", sortType);
            m_Reversed = getSharedPreferences(PREF_FILE, MODE_PRIVATE).getBoolean("SORT_ORDER", false);
            m_View = getSharedPreferences(PREF_FILE, MODE_PRIVATE).getInt("VIEW_BY", m_View);
        } catch (Exception ex) {
            Log.e(LOGTAG, "preference exception: ", ex);

        }
        ScannedFile.setSortType(sortType);
        ScannedFile.setSortReversed(m_Reversed);
        super.readSettings();
    }

    @Override
    public void onResume() {
        if (!m_FirstTime && !m_ScanInProgress && m_View ==0 && ScannedFile.getSortType() == ScannedFile.SORT_BY_LATEST) {
            SortTask task = new SortTask(true);
            task.execute(ScannedFile.SORT_BY_LATEST);
        }
        if( !m_FirstTime && !m_ScanInProgress) {
            if( m_ShowPageNumbers) {
                PageTask p = new PageTask(true);
                p.execute();
            } else {
                m_PageNumbersUpdated=false;
            }
        }
        if( m_PrevIntent != null) {
            Intent tmp = getReadingNow();
            String data = tmp.getDataString();
            if( data != null && data.startsWith("file:///data/data/com.nookdevs.library/files/MyBooks")) {
                updateReadingNow(m_PrevIntent);
            }
        }
        super.onResume();
    }
    @Override
    public void onDestroy() {
        try {
            if( m_OtherBooks != null)
                m_OtherBooks.close();
            if( m_FictionwiseBooks != null)
                m_FictionwiseBooks.close();
            if( m_Smashwords != null)
                m_Smashwords.close();
            if( m_BNBooks != null)
                m_BNBooks.close();
        } catch(Exception ex) {
            Log.e(LOGTAG, ex.getMessage(), ex);
        }
        super.onDestroy();
    }

    public void refreshPage() {
        m_Handler.post( new Runnable() {
            public void run() {
                pageViewHelper.gotoItem( pageViewHelper.getCurrentIndex());
            }
        });
    }
    void updatePageView(List files) {
        if (files != null && files.size() > 0) {
            synchronized (m_Files) {
                m_Files.addAll(files);
            }
            updatePageView(false);
        }
    }

    private void updatePageView(final boolean last) {
        List<String> tmpList = null;
        List<String> authList =null;
        if (last) {
            Collections.sort(m_Files);
            tmpList = getAvailableKeywords();
            m_ShowIndex = 0;
            m_AuthorIndex = 0;
            authList = ScannedFile.getAuthors();
            Comparator<String> c = new Comparator<String>() {
                public int compare(String object1, String object2) {
                    if (object1 != null) {
                        return object1.compareToIgnoreCase(object2);
                    } else {
                        return 1;
                    }
                }
            };
            Collections.sort(tmpList, c);
            Collections.sort(authList, c);
            m_ShowValues = new ArrayList<CharSequence>(tmpList.size() + 1);
            m_AuthorValues = new ArrayList<String>(authList.size() + 1);
        } else {
            m_ShowValues = new ArrayList<CharSequence>(1);
            m_AuthorValues = new ArrayList<String>(1);
        }
        m_ShowIndex = 0;
        m_AuthorIndex = 0;
        m_ShowValues.add(getString(R.string.all));
        m_AuthorValues.add(getString(R.string.all));
        if (ScannedFile.m_StandardKeywords != null) {
            m_ShowValues.addAll(ScannedFile.m_StandardKeywords);
        }
        if (last) {
            m_ShowValues.addAll(tmpList);
            m_AuthorValues.addAll(authList);
        }
        m_ShowAdapter = new ArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem2, m_ShowValues);
        m_AuthorAdapter = new ArrayAdapter<String>(lview.getContext(), R.layout.listitem2, m_AuthorValues);
        Runnable thrd = new Runnable() {
            public void run() {
                closeAlert();
                LinearLayout pageview = (LinearLayout) NookLibrary.this.findViewById(R.id.pageview1);
                if (pageViewHelper == null) {
                    pageViewHelper = new PageViewHelper(NookLibrary.this, pageview, m_Files);
                    pageViewHelper.setShowPageNumbers(m_ShowPageNumbers);
                    String [] folders = { SDFOLDER, EXTERNAL_SDFOLDER};
                    pageViewHelper.setFolders(folders);
                } else {
                    pageViewHelper.setShowPageNumbers(m_ShowPageNumbers);
                    pageViewHelper.setFiles(m_Files);
                }
                m_ListAdapter.setSubText(SORT_BY, m_SortMenuValues.get(ScannedFile.getSortType()).toString());
                m_ListAdapter.setSubText(SHOW, m_ShowValues.get(0).toString());
                m_ListAdapter.setSubText(AUTHORS, m_AuthorValues.get(0).toString());
                m_ListAdapter.setSubText(SEARCH, " ");
                m_ListAdapter.setSubText(SHOW_ARCHIVED, getString(R.string.off));
                if( last) {
                    m_ListAdapter.setSubText(VIEW_BY, m_ViewMenuValues.get(m_View).toString());
                    pageViewHelper.setView(m_View);
                }

            }
        };
        m_Handler.post(thrd);

    }

    private void queryFolders() {
        queryFolders(LOCAL_BOOKS);
    }

    private void queryFolders(final int type) {
        if (m_ScanInProgress) { return; }
        m_ScanInProgress = true;
        ScannedFile.loadStandardKeywords();
        displayAlert(getString(R.string.scanning), getString(R.string.please_wait), 1, null, -1);
        m_ListAdapter.setSubText(REFRESH, getString(R.string.in_progress));
        m_Files.clear();
        m_Lock.acquire();
        m_LocalScanDone.close();
        Runnable thrd1 = new Runnable() {
            public void run() {
                m_BNBooksLock.close();
                List<ScannedFile> files = m_BNBooks.getBooks(type == BN_BOOKS || type == ALL_BOOKS);
                try {
                    if( files != null) {
                        for (ScannedFile file : files) {
                            if( file == null) continue;
                            file.loadCover(m_Lock);
                        }
                    }
                } catch(Exception ex) {
                    
                } finally {
                    if( m_Lock.isHeld())
                        m_Lock.release();
                }
                updatePageView(files);
                m_BNBooks.clear();
                m_BNBooksLock.open();
            }
        };
        (new Thread(thrd1)).start();
        Runnable thrd = new Runnable() {
            public void run() {
                m_OtherBooks.getOtherBooks();
                updateMetaData(true,0);
                m_LocalScanDone.block();
                m_BNBooksLock.block();
                m_FictionwiseLock.block();
                m_SmashwordsLock.block();
                if( m_ShowPageNumbers) {
                    m_OtherBooks.updatePageNumbers(m_Files);
                    m_PageNumbersUpdated=true;
                } else {
                    m_PageNumbersUpdated=false;
                }
                updatePageView(true);
                loadCovers();
            }
        };
        final CharSequence val = m_RefreshAdapter.getItem(type);
        (new Thread(thrd)).start();
        if (m_FictionwiseBooks.getUser()) {
            Runnable thrd2 = new Runnable() {
                public void run() {
                    m_FictionwiseLock.close();
                    m_FictionwiseBooks.getBooks(val.equals(getString(R.string.fictionwise)) || type == ALL_BOOKS);
                    m_FictionwiseLock.open();
                }
            };
            (new Thread(thrd2)).start();
        } else {
            m_FictionwiseLock.open();
        }
        if (m_Smashwords.getUser()) {
            Runnable thrd3 = new Runnable() {
                public void run() {
                    m_SmashwordsLock.close();
                    m_Smashwords.getBooks(val.equals(getString(R.string.smashwords)) || type == ALL_BOOKS);
                    m_SmashwordsLock.open();
                }
            };
            (new Thread(thrd3)).start();
        } else {
            m_SmashwordsLock.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private Dialog m_Dialog = null;
    private OnKeyListener m_TextListener = new OnKeyListener() {

        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (view instanceof EditText) {
                    EditText editTxt = (EditText) view;
                    if (keyCode == SOFT_KEYBOARD_CLEAR) {
                        editTxt.setText("");
                    } else if (keyCode == SOFT_KEYBOARD_SUBMIT) {
                        String text = editTxt.getText().toString();
                        m_Dialog.cancel();
                        if (!text.trim().equals("")) {
                            SearchTask task = new SearchTask();
                            task.execute(text);
                        }
                    } else if (keyCode == SOFT_KEYBOARD_CANCEL) {
                        m_Dialog.cancel();
                    }
                }
            }
            return false;
        }

    };

    private OnKeyListener m_ScreenSaverFolderNameInputListener = new OnKeyListener() {
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (view instanceof EditText) {
                    EditText editTxt = (EditText) view;
                    if (keyCode == SOFT_KEYBOARD_CLEAR) {
                        editTxt.setText("");
                    } else if (keyCode == SOFT_KEYBOARD_SUBMIT) {
                        String screenSaverName = editTxt.getText().toString();
                        m_Dialog.cancel();
                        Editor prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
                        screenSaverName = screenSaverName.replaceAll("[\\s/\\\\]+", " ").trim();
                        if (!screenSaverName.equals("")) {
                            prefs.putString(PREFS_KEY_SCREENSAVER_FOLDER_NAME, screenSaverName);
                            m_ScreenSaverAdapter.setSubText(0, screenSaverName);
                        } else {
                            prefs.remove(PREFS_KEY_SCREENSAVER_FOLDER_NAME);
                            m_ScreenSaverAdapter.setSubText(0, "(disabled)");
                        }
                        prefs.commit();
                        updateScreenSaverMenuItem();
                    } else if (keyCode == SOFT_KEYBOARD_CANCEL) {
                        m_Dialog.cancel();
                    }
                }
            }
            return false;
        }
    };
    private class AddLibListener implements OnKeyListener {
        EditText m_User;
        EditText m_Pass;
        private int m_Type;

        public AddLibListener(int type, EditText user, EditText pass) {
            m_Type = type;
            m_User = user;
            m_Pass = pass;
        }

        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (view instanceof EditText) {
                    EditText editTxt = (EditText) view;
                    if (keyCode == SOFT_KEYBOARD_CLEAR) {
                        editTxt.setText("");
                    } else if (keyCode == SOFT_KEYBOARD_SUBMIT) {
                        String login = m_User.getText().toString();
                        String pass = m_Pass.getText().toString();
                        if (login != null && pass != null) {
                            m_Dialog.cancel();
                            if (m_Type == FICTIONWISE_BOOKS) {
                                if (!m_FictionwiseBooks.addUser(login, pass)) {
                                    displayAlert(getString(R.string.add_lib), getString(R.string.add_lib_error), 2,
                                        null, -1);
                                } else {
                                    String name = getString(R.string.fictionwise);
                                    m_LibsAdapter.remove(name);
                                    m_RefreshAdapter.insert(name, m_AddLibIndex);
                                    queryFolders(m_AddLibIndex);
                                    m_AddLibIndex++;

                                }
                            } else if (m_Type == SMASHWORDS) {
                                if (!m_Smashwords.addUser(login, pass)) {
                                    displayAlert(getString(R.string.add_lib), getString(R.string.add_lib_error), 2,
                                        null, -1);
                                } else {
                                    String name = getString(R.string.smashwords);
                                    m_LibsAdapter.remove(name);
                                    m_RefreshAdapter.insert(name, m_AddLibIndex);
                                    queryFolders(m_AddLibIndex);
                                    m_AddLibIndex++;

                                }
                            }
                        } else {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                    } else if (keyCode == SOFT_KEYBOARD_CANCEL) {
                        m_Dialog.cancel();
                    }
                }
            }
            return false;
        }

    };

    protected void displayDialog(int cmd) {
        if (m_Dialog == null) {
            m_Dialog = new Dialog(this, android.R.style.Theme_Panel);
        }
        m_Dialog.setCancelable(true);
        m_Dialog.setCanceledOnTouchOutside(true);
        if (cmd == SEARCH) {
            m_Dialog.setContentView(R.layout.textinput);
            TextView txt = (TextView) m_Dialog.findViewById(R.id.TextView01);
            EditText keyword = (EditText) m_Dialog.findViewById(R.id.EditText01);
            txt.setText(R.string.search_lib);
            keyword.setText("");
            keyword.requestFocus();
            keyword.setOnKeyListener(m_TextListener);
       } else if (cmd == SCREENSAVER) {
            m_Dialog.setContentView(R.layout.textinput);
            TextView txt = (TextView) m_Dialog.findViewById(R.id.TextView01);
            EditText keyword = (EditText) m_Dialog.findViewById(R.id.EditText01);
            txt.setText(R.string.cover_screensaver_name);
            SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            keyword.setText(prefs.getString(PREFS_KEY_SCREENSAVER_FOLDER_NAME, "")
                                 .replaceAll("[\\s/\\\\]+", " ")
                                 .trim());
            keyword.requestFocus();
            keyword.setOnKeyListener(m_ScreenSaverFolderNameInputListener);
        } else {
            m_Dialog.setContentView(R.layout.library_input);
            TextView txt = (TextView) m_Dialog.findViewById(R.id.libtext01);
            txt.setText(m_LibsAdapter.getItem(cmd));
            EditText user = (EditText) m_Dialog.findViewById(R.id.libedittext02);
            user.requestFocus();
            EditText pass = (EditText) m_Dialog.findViewById(R.id.libedittext03);
            AddLibListener l = new AddLibListener(cmd, user, pass);
            user.setOnKeyListener(l);
            pass.setOnKeyListener(l);
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        m_Handler.postDelayed(new Runnable() {
            public void run() {
                m_Dialog.show();
            }
        }, 400);
    }
    private void loadCovers() {
        loadCovers(false);
    }
    private void loadCovers(boolean archive) {
        Log.e(LOGTAG, "Updating covers");
        List<ScannedFile> list = m_Files;
        try {
            for (ScannedFile file : list) {
                if( file == null) continue;
                file.loadCover(m_Lock);
            }
        } catch(Exception ex) {
            Log.e(LOGTAG, "Exception while loading cover -" + ex.getMessage(), ex);
        }
        if( m_Lock.isHeld())
            m_Lock.release();
        m_FictionwiseBooks.close();
        final List<String> tmpList = getAvailableKeywords();
        final List<String> authList = ScannedFile.getAuthors();
        Comparator<String> c = new Comparator<String>() {
            public int compare(String object1, String object2) {
                if (object1 != null) {
                    return object1.compareToIgnoreCase(object2);
                } else {
                    return 1;
                }
            }

        };
        Collections.sort(tmpList, c);
        Collections.sort(authList, c);
        Runnable thrd = new Runnable() {
            public void run() {
                m_ShowValues = new ArrayList<CharSequence>(tmpList.size() + 1);
                m_AuthorValues = new ArrayList<String>(authList.size() + 1);
                m_AuthorValues.add(getString(R.string.all));
                m_AuthorValues.addAll(authList);
                m_ShowValues.add(getString(R.string.all));
                if (ScannedFile.m_StandardKeywords != null) {
                    m_ShowValues.addAll(ScannedFile.m_StandardKeywords);
                }
                m_ShowValues.addAll(tmpList);
                m_ShowAdapter = new ArrayAdapter<CharSequence>(lview.getContext(), R.layout.listitem2, m_ShowValues);
                m_ListAdapter.setSubText(REFRESH, " ");
                m_AuthorAdapter = new ArrayAdapter<String>(lview.getContext(), R.layout.listitem2, m_AuthorValues);
                m_ScanInProgress = false;
            }
        };
        m_Handler.post(thrd);
    }
    protected List<CharSequence> getShowValues() {
        return m_ShowValues;
    }
    private Vector<Object> loadImages() {
        Vector<Object> result = new Vector<Object>();
        List<ScannedFile> list = pageViewHelper.getFiles();
        for (ScannedFile file : list) {
            String cover = file.getCover();
            if( cover == null) {
                if( file.getCoverId() ==0) {
                    String ext = file.getType();
                    if( ext == null || ext.trim().equals("")) {
                        file.setCoverId(R.drawable.folder);
                    } else if( ext.equalsIgnoreCase("pdf")) {
                        file.setCoverId(R.drawable.pdf);
                    }
                    else if( ext.equalsIgnoreCase("txt")) {
                        file.setCoverId(R.drawable.txt);
                    }
                    else if( ext.equalsIgnoreCase("htm") || ext.equalsIgnoreCase("html")) {
                        file.setCoverId(R.drawable.html);
                    }
                }
                if( file.getCoverId() !=0) {
                    result.add( new Integer(file.getCoverId()));
                } else {
                    result.add(null);
                }
            } else {
                if( cover.startsWith("http")) {
                    result.add(new Integer(R.drawable.no_cover));
                } else
                    result.add(cover);
            }
        }
        return result;
    }

    public void onItemClick(AdapterView<?> v, View parent, int position, long id) {
        if (v.equals(submenu)) {
            if (m_SubMenuType == SORT_BY) {
                int currvalue = ScannedFile.getSortType();
                subicons[currvalue] = -1;
                subicons[SORT_REVERSE_ORDER] = -1;
                if (currvalue != position) {
                    if( position == SORT_REVERSE_ORDER) {
                        m_Reversed = !m_Reversed;
                        Editor e = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
                        e.putBoolean("SORT_ORDER", m_Reversed);
                        e.commit();
                        ScannedFile.setSortReversed(m_Reversed);
                        position = ScannedFile.getSortType();
                    } else {
                        m_ListAdapter.setSubText(SORT_BY, m_SortMenuValues.get(position).toString());
                        Editor e = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
                        e.putInt("SORT_BY", position);
                        e.commit();
                    }
                    SortTask task = new SortTask();
                    task.execute(position);
                }
            } else if (m_SubMenuType == SHOW) {
                if (m_ShowIndex != position) {
                    m_ListAdapter.setSubText(SHOW, (String) m_ShowValues.get(position));
                    ShowTask task = new ShowTask();
                    m_ShowIndex = position;
                    task.execute((String) m_ShowValues.get(position));
                }
            } else if (m_SubMenuType == AUTHORS) {
                if (m_AuthorIndex != position) {
                    m_ListAdapter.setSubText(AUTHORS, m_AuthorValues.get(position));
                    ShowTask task = new ShowTask();
                    m_AuthorIndex = position;
                    task.execute((String) m_ShowValues.get(m_ShowIndex));
                }
            } else if (m_SubMenuType == REFRESH) {
                if (position == m_AddLibIndex) {
                    submenu.setAdapter(m_LibsAdapter);
                    animator.setInAnimation(this, R.anim.fromleft);
                    animator.showNext();
                    animator.setInAnimation(this, R.anim.fromright);
                    animator.showNext();
                    m_SubMenuType = ADD_LIB_MENU;
                    return;
                } else {
                    queryFolders(position);
                }
            } else if (m_SubMenuType == SCREENSAVER) {
                if (position == 0) {  // edit screen saver folder name
                    displayDialog(SCREENSAVER);
                    return;
                } else {  // change image count
                    m_ScreenSaverImageCount = -1 + (2 * position);
                    for (int i = 1; i <= 3; i++) {
                        screensavericons[i] = (i == position ? R.drawable.check_image : -1);
                    }
                    Editor prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
                    prefs.putInt(PREFS_KEY_SCREENSAVER_IMAGE_COUNT, m_ScreenSaverImageCount);
                    prefs.commit();
                    updateScreenSaverMenuItem();
                }
            } else if (m_SubMenuType == ADD_LIB_MENU) {
                displayDialog(position);
            } else if( m_SubMenuType == VIEW_BY) {
                viewicons[m_View] = -1;
                if (m_View != position) {
                    m_ListAdapter.setSubText(VIEW_BY, m_ViewMenuValues.get(position).toString());
                    m_View = position;
                    pageViewHelper.setFiles(m_Files);
                    pageViewHelper.setView(m_View);
                    Editor e = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
                    e.putInt("VIEW_BY", position);
                    e.commit();
                }
            }
            animator.setInAnimation(this, R.anim.fromleft);
            animator.showNext();
            m_SubMenuType = -1;
            return;
        }
        // main menu actions here.
        switch (position) {
            case VIEW_DETAILS:
                if( m_View >1) {
                    displayAlert(R.string.cover_flow_error);
                    break;
                }
                ScannedFile file = pageViewHelper.getCurrent();
                if( file.getCoverId() == R.drawable.folder) {
                    displayAlert(R.string.not_valid_error);
                    break;
                }
                m_PageViewAnimator.showNext();
                m_Details.setVisibility(View.VISIBLE);
                m_DetailsScroll.setVisibility(View.VISIBLE);
                m_CoverBtn.setVisibility(View.VISIBLE);
                String tmp = file.getCover();
                if (tmp == null) {
                    if( file.getCoverId() ==0) {
                        String ext = file.getType();
                        if( ext.equalsIgnoreCase("pdf"))
                            file.setCoverId(R.drawable.pdf);
                        if( ext.equalsIgnoreCase("txt"))
                            file.setCoverId(R.drawable.txt);
                        if( ext.equalsIgnoreCase("htm") || ext.equalsIgnoreCase("html"))
                            file.setCoverId(R.drawable.html);
                    }
                    if( file.getCoverId() !=0) {
                        m_CoverBtn.setImageResource( file.getCoverId());                        }
                    else
                        m_CoverBtn.setImageResource(R.drawable.no_cover);
                } else {
                    m_CoverBtn.setImageURI(Uri.parse(tmp));
                }
              //  if (!file.matchSubject("B&N")) {
                    m_Archive.setVisibility(View.VISIBLE);
              //  }
                Spanned txt = Html.fromHtml(file.getDetails());
                m_Details.setText(txt);
                m_DetailsPage.setText(txt);
                lview.setVisibility(View.INVISIBLE);
                upButton.setVisibility(View.INVISIBLE);
                downButton.setVisibility(View.INVISIBLE);
                goButton.setVisibility(View.INVISIBLE);
                m_SubMenuType = VIEW_DETAILS;
                break;
            case SEARCH:
                if( m_View !=0) {
                    displayAlert();
                } else
                    displayDialog(SEARCH);
                break;
            case SORT_BY:
                if( m_View !=0) {
                    displayAlert();
                    break;
                }
                int currvalue = ScannedFile.getSortType();
                subicons[currvalue] = R.drawable.check_image;
                subicons[SORT_REVERSE_ORDER] = m_Reversed?R.drawable.check:-1;
                m_SortAdapter.setIcons(subicons);
                submenu.setAdapter(m_SortAdapter);
                animator.setInAnimation(this, R.anim.fromright);
                animator.showNext();
                m_SubMenuType = SORT_BY;
                break;
            case SHOW:
                if( m_View !=0) {
                    displayAlert();
                    break;
                }
                submenu.setAdapter(m_ShowAdapter);
                animator.setInAnimation(this, R.anim.fromright);
                animator.showNext();
                m_SubMenuType = SHOW;
                break;
            case AUTHORS:
                if( m_View !=0) {
                    displayAlert();
                    break;
                }
                submenu.setAdapter(m_AuthorAdapter);
                animator.setInAnimation(this, R.anim.fromright);
                animator.showNext();
                m_SubMenuType = AUTHORS;
                break;
            case VIEW_BY:
                if (m_ScanInProgress) { return; }
                if( m_ArchiveView) { return; }
                viewicons[m_View] = R.drawable.check_image;
                submenu.setAdapter(m_ViewAdapter);
                animator.setInAnimation(this, R.anim.fromright);
                animator.showNext();
                m_SubMenuType = VIEW_BY;
                break;
            case SHOW_ARCHIVED:
                if (m_ScanInProgress) { return; }
                if( m_View !=0) {
                    displayAlert();
                    break;
                }
                if (m_ArchiveView) {
                    m_ArchiveView = false;
                    pageViewHelper.setTitle(R.string.my_documents);
                    queryFolders();
                    m_ListAdapter.setSubText(SHOW_ARCHIVED, getString(R.string.off));
                    break;
                }
                displayAlert(getString(R.string.scanning), getString(R.string.please_wait), 1, null, -1);
                m_ScanInProgress=true;
                m_ListAdapter.setSubText(SHOW_ARCHIVED, getString(R.string.on));
                m_ArchiveView = true;
                pageViewHelper.setTitle(R.string.archived_books);
                m_Files.clear();
                m_Files.addAll(m_OtherBooks.getArchived());
                m_Files.addAll(m_FictionwiseBooks.getArchived());
                m_Files.addAll(m_Smashwords.getArchived());
                m_Files.addAll(m_BNBooks.getArchived());
                m_ShowIndex = 0;
                m_AuthorIndex = 0;
                m_SearchView = false;
                m_LocalScanDone.close();
                updateMetaData(true,0);
                m_LocalScanDone.block();
                m_ListAdapter.setSubText(AUTHORS, m_AuthorValues.get(0));
                m_ListAdapter.setSubText(SHOW, (String) m_ShowValues.get(0));
                m_ListAdapter.setSubText(SEARCH, " ");
                SortTask task = new SortTask();
                task.execute(ScannedFile.getSortType());
                ScannedFile.loadStandardKeywords();
                loadCovers(true);
                closeAlert();
                break;
            case SCREENSAVER: {
                if( m_View !=0) {
                    displayAlert();
                    break;
                }
                SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                String screenSaverName =
                    prefs.getString(PREFS_KEY_SCREENSAVER_FOLDER_NAME, "(disabled)")
                         .replaceAll("[\\s/\\\\]+", " ")
                         .trim();
                m_ScreenSaverAdapter.setSubText(0, screenSaverName);
                int n = prefs.getInt(PREFS_KEY_SCREENSAVER_IMAGE_COUNT, 5);
                for (int i = 1; i <= 3; i++) {
                    screensavericons[i] = -1;
                }
                if (n == 1) {
                    screensavericons[1] = R.drawable.check_image;
                } else if (n == 3) {
                    screensavericons[2] = R.drawable.check_image;
                } else if (n == 5) {
                    screensavericons[3] = R.drawable.check_image;
                }
                m_ScreenSaverAdapter.setIcons(screensavericons);
                submenu.setAdapter(m_ScreenSaverAdapter);
                animator.setInAnimation(this, R.anim.fromright);
                animator.showNext();
                m_SubMenuType = SCREENSAVER;
                break;
            }
            case PAGE_NUMBERS:
                m_ShowPageNumbers = !m_ShowPageNumbers;
                Editor e = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
                e.putBoolean("PAGE_NUMBERS", m_ShowPageNumbers);
                e.commit();
                pageViewHelper.setShowPageNumbers(m_ShowPageNumbers);
                if( m_ShowPageNumbers && !m_PageNumbersUpdated) {
                    m_OtherBooks.updatePageNumbers(m_Files);
                    m_BNBooks.updatePageNumbers();
                    m_PageNumbersUpdated=false;
                }
                m_ListAdapter.setSubText(PAGE_NUMBERS, m_ShowPageNumbers?getString(R.string.on):getString(R.string.off));
                break;
            case HELP:
                try {
                    Intent intent = new Intent("com.bravo.intent.action.VIEW");
                    File f = new File("/data/data/com.nookdevs.library/files/MyBooks"+m_Version+".epub");
                    if( !f.exists()) {
                        InputStream is = getAssets().open("MyBooks.epub");
                        OutputStream fout =openFileOutput(f.getName(), MODE_WORLD_READABLE);
                        byte[] buf = new byte[1024];
                        int len=0;
                        while( (len=is.read(buf)) > 0) {
                            fout.write(buf, 0, len);
                        }
                        fout.close();
                        is.close();
                    }
                    m_PrevIntent=getReadingNow();
                    intent.setDataAndType(Uri.fromFile(f), "application/epub");
                    startActivity(intent);
                } catch(Exception ex) {
                    Log.e(LOGTAG, "Help:" + ex.getMessage(), ex);
                }
                break;
            case CLOSE:
                goHome();
                break;
            case REFRESH:
                if( m_View !=0) {
                    displayAlert();
                    break;
                }
                if (m_ScanInProgress || m_ArchiveView) { return; }
                submenu.setAdapter(m_RefreshAdapter);
                animator.setInAnimation(this, R.anim.fromright);
                animator.showNext();
                m_SubMenuType = REFRESH;
                break;
            case SHOW_COVERS:
                if( m_View >1) {
                    displayAlert(R.string.cover_flow_error);
                    break;
                }
                Vector<Object> images = loadImages();
                m_IconAdapter.setImageUrls(images);
                m_IconGallery.setAdapter(m_IconAdapter);
                m_IconGallery.setVisibility(View.VISIBLE);
                m_CloseBtn.setVisibility(View.VISIBLE);
                lview.setVisibility(View.INVISIBLE);
                backButton.setVisibility(View.INVISIBLE);
                upButton.setVisibility(View.INVISIBLE);
                downButton.setVisibility(View.INVISIBLE);
                goButton.setVisibility(View.INVISIBLE);
                m_IconGallery.setSelection(pageViewHelper.getCurrentIndex() - 1, true);

                break;
        }

    }

    public void onClick(View button) {
        if (button.equals(m_CloseBtn)) {
            if (m_Toast != null) {
                m_Toast.cancel();
                m_Toast.getView().setVisibility(View.INVISIBLE);
            }
            m_CloseBtn.setVisibility(View.INVISIBLE);
            m_GalleryTitle.setVisibility(View.INVISIBLE);
            m_IconGallery.setVisibility(View.INVISIBLE);
            backButton.setVisibility(View.VISIBLE);
            if( m_SubMenuType != VIEW_DETAILS) {
                lview.setVisibility(View.VISIBLE);
                upButton.setVisibility(View.VISIBLE);
                downButton.setVisibility(View.VISIBLE);
                goButton.setVisibility(View.VISIBLE);
            }
        }
        if (button.equals(backButton)) {
            if (m_SubMenuType == VIEW_DETAILS) {
                lview.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                upButton.setVisibility(View.VISIBLE);
                downButton.setVisibility(View.VISIBLE);
                goButton.setVisibility(View.VISIBLE);
                m_CoverBtn.setVisibility(View.INVISIBLE);
                m_Details.setVisibility(View.INVISIBLE);
                m_DetailsScroll.setVisibility(View.INVISIBLE);
                m_Archive.setVisibility(View.INVISIBLE);
                m_PageViewAnimator.showNext();
                m_SubMenuType = -1;
                return;
            } else if (m_SubMenuType >= 0) {
                animator.setInAnimation(this, R.anim.fromleft);
                animator.showNext();
                m_SubMenuType = -1;
                return;
            } else if (m_SearchView) {
                m_SearchView = false;
                pageViewHelper.setTitle(R.string.my_documents);
                m_ListAdapter.setSubText(SEARCH, " ");
                if (m_ShowIndex != 0) {
                    ShowTask task = new ShowTask();
                    task.execute(m_ShowValues.get(m_ShowIndex).toString());
                } else {
                    pageViewHelper.setFiles(m_Files);
                }
                return;
            } else if (m_ArchiveView) {
                m_ArchiveView = false;
                pageViewHelper.setTitle(R.string.my_documents);
                queryFolders();
                m_ListAdapter.setSubText(SHOW_ARCHIVED, getString(R.string.off));
                return;
            } else if( m_View == PageViewHelper.FOLDERS) {
                if( m_Level >0) {
                    m_Level--;
                    if( m_Level ==0) {
                        String[] folders = { SDFOLDER, EXTERNAL_SDFOLDER};
                        pageViewHelper.setFolders(folders);
                        pageViewHelper.setView(m_View, true);
                    } else {
                        File file = pageViewHelper.getCurrentFolder();
                        file = file.getParentFile();
                        String [] f = { file.getAbsolutePath()};
                        pageViewHelper.setFolders(f);
                        pageViewHelper.setView(m_View, true);
                    }
                    return;
                }
            } else if( m_View == PageViewHelper.TAGS) {
                if( m_Level >0) {
                    m_Level--;
                    m_ListAdapter.setSubText(SHOW, m_ShowValues.get(0).toString());
                    pageViewHelper.setFiles(m_Files);
                    pageViewHelper.setView(m_View, true);
                    pageViewHelper.gotoItem(m_ShowIndex+1);
                    m_ShowIndex=0;
                    return;
                }
            }
            goHome();
        } else if (button.equals(upButton)) {
            pageViewHelper.selectPrev();
        } else if (button.equals(downButton)) {
            pageViewHelper.selectNext();
        } else if (button.equals(goButton) || button.equals(m_CoverBtn)) {
            if (m_ArchiveView) {
                onItemClick(lview, null,VIEW_DETAILS,-1);
                return;
            }
            final ScannedFile file = pageViewHelper.getCurrent();
            String path = file.getPathName();
            if( m_View == PageViewHelper.FOLDERS) {
                File folder = pageViewHelper.getCurrentFolder();
                String tmp = path;
                if( tmp != null && tmp.charAt(0) == '[') {
                    tmp = tmp.substring(1,tmp.length()-1);
                    File f = new File( folder, tmp);
                    String[] folders = { f.getAbsolutePath() };
                    m_Level++;
                    pageViewHelper.setFolders(folders);
                    pageViewHelper.setView(m_View, true);
                    return;
                } else if( tmp != null && tmp.charAt(0) =='/') {
                    File f = new File(tmp);
                    if( f.isDirectory()) {
                        m_Level++;
                        String[] folders = { folder.getAbsolutePath() };
                        pageViewHelper.setFolders(folders);
                        pageViewHelper.setView(m_View, true);
                        return;
                    }
                } else {
                    path = folder.getAbsolutePath() + "/" + path;
                }
            } else if( m_View == PageViewHelper.TAGS && m_ShowIndex ==0) {
                m_Level=1;
                m_ListAdapter.setSubText(SHOW, (String) file.getPathName());
                ShowTask task = new ShowTask();
                m_ShowIndex = pageViewHelper.getCurrentIndex()-1;
                task.execute(file.getPathName());
                return;
            } else {
                if (file.getStatus() != null && !file.getStatus().equals(BNBooks.BORROWED)
                    && !file.getStatus().equals(BNBooks.SAMPLE)) {
                    int msgId;
                    if (file.getStatus().startsWith(BNBooks.DOWNLOAD_IN_PROGRESS)) {
                        msgId = R.string.download_in_progress;
                    }
                    if (file.getStatus().contains(BNBooks.DOWNLOAD)) {
                        file.setStatus(BNBooks.DOWNLOAD_IN_PROGRESS);
                        Runnable thrd = new Runnable() {
                            public void run() {
                                if (file.getBookID() != null) {
                                    if (file.matchSubject(getString(R.string.fictionwise))) {
                                        m_FictionwiseBooks.downloadBook(file);
                                    } else {
                                        m_Smashwords.downloadBook(file);
                                    }
                                } else {
                                    m_BNBooks.getBook(file);
                                }
                                m_Handler.post(new Runnable() {
                                    public void run() {
                                        pageViewHelper.update();
                                    }
                                });
                            }
                        };
                        (new Thread(thrd)).start();
                        msgId = R.string.download_started;
                        pageViewHelper.update();
                    } else {
                        msgId = R.string.on_loan;
                    }
                    Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (path == null) { return; }
            Intent intent = new Intent("com.bravo.intent.action.VIEW");
            String mimetype = "application/";
            int idx = path.lastIndexOf('.');
            // File file1 = new File(path);
            // file1.setLastModified(System.currentTimeMillis());
            String ext = path.substring(idx + 1).toLowerCase();
            //  For text files, launch mTextView, if it's installed:
            if ("txt".equals(ext)) {
                try {
                    File tfile = new File(path);
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.ACTION_DEFAULT);
                    intent.setComponent(new ComponentName("com.nookdevs.mtextview", "com.nookdevs.mtextview.ViewerActivity"));
                    intent.setData(Uri.fromFile(tfile));
                    startActivity(intent);
                    return;
                } catch (Exception ex) {
                    intent = new Intent("com.bravo.intent.action.VIEW");
                }
            }
            if ("txt".equals(ext) || "html".equals(ext) || "htm".equals(ext)) {
                // try nookBrowser first
                try {
                    File tfile = new File(path);
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.ACTION_DEFAULT);
                    intent.setComponent(new ComponentName("com.nookdevs.browser", "com.nookdevs.browser.nookBrowser"));
                    intent.setData(Uri.fromFile(tfile));
                    startActivity(intent);
                    return;
                } catch (Exception ex) {
                    intent = new Intent("com.bravo.intent.action.VIEW");
                }
            }

            if (path.endsWith("fb2.zip") || ext.equals("fb2")) {
                mimetype += "fb2";
            } else {
                mimetype += ext;
            }
            path = "file://" + path;
            if (file.getEan() != null && !file.getEan().trim().equals("")) {
                path += "?EAN=" + file.getEan();
            }
            System.out.println("Trying to open Path =" + path);
            intent.setDataAndType(Uri.parse(path), mimetype);
            updateReadingNow(intent);
            file.updateLastAccessDate();
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Log.i(LOGTAG, "Error while attempting to start reader App", ex);
                Toast.makeText(this, R.string.reader_not_found, Toast.LENGTH_LONG).show();
            }

            // generate a screen saver image depicting recent covers, if the corresponding
            // properties are set...
            SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            String screenSaverFolderName = prefs.getString(PREFS_KEY_SCREENSAVER_FOLDER_NAME, "")
                                                .replaceAll("[\\s/\\\\]+", " ")
                                                .trim();
            int n = prefs.getInt(PREFS_KEY_SCREENSAVER_IMAGE_COUNT, 5);
            if (screenSaverFolderName.length() > 0 && n > 0) {
                // NOTE: Choose a random name for the cover, else the screen saver app will cache
                //       the image by name and not notice its being replaced.
                File screenSaverImage =
                    new File("/system/media/sdcard/my screensavers/" + screenSaverFolderName +
                             "/cover.jpg");
                String coverImage = file.getOriginalCover();
                if (coverImage != null && !coverImage.startsWith("http://")) {
                    screenSaverImage.getParentFile().mkdir();
                    createScreenSaverOfCovers(new File(coverImage),
                                              screenSaverImage,
                                              Math.min(n, 5));
                }
            }
        }
    }

    private void createScreenSaverOfCovers(File currentCoverFile, File destinationFile, int limit) {
        // determine further covers to display underneath the current cover...
        List<File> coverFiles = new ArrayList<File>(limit);
        coverFiles.add(currentCoverFile);
        Iterator<ScannedFile> it;

        // sort the list of books by access date...
        Comparator<ScannedFile> c = new Comparator<ScannedFile>() {
           public int compare(ScannedFile sf1, ScannedFile sf2) {
               Date d1 = sf1.getLastAccessedDate();
               Date d2 = sf2.getLastAccessedDate();
               if (d1 != null && d2 != null) {
                   return -d1.compareTo(d2);
               } else if (d1 != null) {
                   return -1;
               } else if (d2 != null) {
                   return 1;
               }
               return 0;
           }
        };
        Collection<ScannedFile> books = new TreeSet<ScannedFile>(c);
        books.addAll(pageViewHelper.getFiles());
        it = books.iterator();

        // determine the covers to be included in the screen saver of covers...
        while (coverFiles.size() < limit && it.hasNext()) {
           String coverFile = it.next().getOriginalCover();
           if (coverFile != null && !coverFile.equals(currentCoverFile.getPath())) {
               coverFiles.add(new File(coverFile));
           }
        }

        // run the screen saver image generation in an AsyncTask...
        Thread t =
            new ScreenSaverImageGenerationThread(getApplicationContext(),
                                                 destinationFile,
                                                 coverFiles.toArray(new File[coverFiles.size()]));
        t.start();
    }

    private void updateScreenSaverMenuItem() {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String name = prefs.getString(PREFS_KEY_SCREENSAVER_FOLDER_NAME, "")
                           .replaceAll("[\\s/\\\\]+", " ")
                           .trim();
        int n = prefs.getInt(PREFS_KEY_SCREENSAVER_IMAGE_COUNT, 5);
        m_ListAdapter.setSubText(
            SCREENSAVER,
            name.length() > 0
                ? getResources().getQuantityString(R.plurals.cover_images, n, String.valueOf(n))
                : "off");
    }

    // from kbs - trook.projectsource code.
    private final void pageUp() {
        if (m_DetailsPage != null) {
            int cury = m_DetailsPage.getScrollY();
            if (cury == 0) { return; }
            int newy = cury - WEB_SCROLL_PX;
            if (newy < 0) {
                newy = 0;
            }
            m_DetailsPage.scrollTo(0, newy);
        }
    }

    private final void pageDown() {
        if (m_DetailsPage != null) {
            int cury = m_DetailsPage.getScrollY();
            int hmax = m_DetailsPage.getMeasuredHeight() - 100;
            if (hmax < 0) {
                hmax = 0;
            }
            int newy = cury + WEB_SCROLL_PX;
            if (newy > hmax) { return;// newy = hmax;
            }
            if (cury != newy) {
                m_DetailsPage.scrollTo(0, newy);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        switch (keyCode) {
            case NOOK_PAGE_UP_KEY_LEFT:
            case NOOK_PAGE_UP_KEY_RIGHT:
            case NOOK_PAGE_UP_SWIPE:
                if (m_SubMenuType == VIEW_DETAILS) {
                    pageUp();
                } else {
                    pageViewHelper.pageUp();
                }
                handled = true;
                break;

            case NOOK_PAGE_DOWN_KEY_LEFT:
            case NOOK_PAGE_DOWN_KEY_RIGHT:
            case NOOK_PAGE_DOWN_SWIPE:
                if (m_SubMenuType == VIEW_DETAILS) {
                    pageDown();
                } else {
                    pageViewHelper.pageDown();
                }
                handled = true;
                break;

            default:
                break;
        }
        if (handled) {
            if (m_CloseBtn.getVisibility() == View.VISIBLE) {
                m_IconGallery.setSelection(pageViewHelper.getCurrentIndex() - 1, true);
            }
        }
        return handled;
    }

    class ShowTask extends AsyncTask<String, Integer, List<ScannedFile>> {
        @Override
        protected void onPreExecute() {
            m_ListAdapter.setSubText(SHOW, getString(R.string.in_progress));
        }

        @Override
        protected List<ScannedFile> doInBackground(String... keyword) {
            try {
                String text = keyword[0];
                List<ScannedFile> list = m_Files;
                if (getString(R.string.all).equals(text)) {
                    return list;
                } else {
                    return filterFiles(text, list);
                }
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ScannedFile> result) {
            m_ListAdapter.setSubText(SHOW, m_ShowValues.get(m_ShowIndex).toString());
            if (m_AuthorIndex != 0) {
                SearchTask task = new SearchTask(result);
                task.setType(AUTHORS);
                String txt = m_AuthorValues.get(m_AuthorIndex);
                task.execute(txt);
            } else if (m_SearchView) {
                SearchTask task = new SearchTask(result);
                String txt = m_ListAdapter.getSubText(SEARCH);
                m_ListAdapter.setSubText(SEARCH, " ");
                task.execute(txt);
            } else if (result != null) {
                pageViewHelper.setFiles(result);
            }
        }

    }

    class SearchTask extends AsyncTask<String, Integer, List<ScannedFile>> {
        private String m_Text;
        List<ScannedFile> m_SubList;
        private int m_Type = SEARCH;

        public SearchTask() {
            super();
            m_SubList = null;
        }

        public SearchTask(List<ScannedFile> list) {
            super();
            m_SubList = list;
        }

        public void setType(int type) {
            m_Type = type;
        }

        @Override
        protected void onPreExecute() {
            if (m_SearchView) {
                m_Text = m_ListAdapter.getSubText(SEARCH);
            } else {
                m_Text = null;
            }
            m_ListAdapter.setSubText(m_Type, getString(R.string.in_progress));
        }

        @Override
        protected List<ScannedFile> doInBackground(String... keyword) {
            try {
                String text = keyword[0];
                List<ScannedFile> list;
                if (m_SubList != null) {
                    list = m_SubList;
                } else {
                    list = pageViewHelper.getFiles();
                }
                if (m_Text == null || m_Text.trim().equals("")) {
                    m_Text = text;
                } else {
                    if (m_Type == AUTHORS) {
                        list = searchFiles(m_Text, list);
                        m_Text = text;
                    } else {
                        m_Text = text + " & " + m_Text;
                    }
                }
                if (m_Type == AUTHORS && text.equals(getString(R.string.all))) { return list; }
                return searchFiles(text, list);
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ScannedFile> result) {
            m_ListAdapter.setSubText(m_Type, m_Text);
            if (result != null) {
                if (m_Type == SEARCH) {
                    pageViewHelper.setTitle(getString(R.string.search_results));
                    m_SearchView = true;
                }
                pageViewHelper.setFiles(result);
            }
        }

    }
    class PageTask extends AsyncTask<Void,Void,Void> {
        boolean resume=true;
        public PageTask() {

        }
        public PageTask(boolean val) {
            resume=val;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            m_OtherBooks.updatePageNumbers(m_Files);
            if( resume)
                m_BNBooks.updatePageNumbers();
            m_PageNumbersUpdated=true;
            return null;
        }
    }

    class SortTask extends AsyncTask<Integer, Integer, List<ScannedFile>> {
        private boolean m_Refresh;

        public SortTask() {
            this(false);
        }

        public SortTask(boolean refresh) {
            m_Refresh = refresh;
        }

        @Override
        protected void onPreExecute() {
            m_ListAdapter.setSubText(SORT_BY, getString(R.string.in_progress));
        }

        @Override
        protected List<ScannedFile> doInBackground(Integer... params) {
            int type = params[0];
            ScannedFile.setSortType(type);
            try {
                if (m_Refresh) {
                    for (ScannedFile file : m_Files) {
                        try {
                            if (file.getPathName() != null) {
                                File f = new File(file.getPathName());
                                file.setLastAccessedDate(new Date(f.lastModified()));
                            }
                        } catch (Exception ex) {
                        }
                    }
                }
                List<ScannedFile> list = pageViewHelper.getFiles();
                if( list != null)
                    Collections.sort(list);
                if (m_SearchView || m_ShowIndex != 0 || m_AuthorIndex != 0) {
                    if( m_Files != null)
                        Collections.sort(m_Files);
                }
                return list;
            } catch (Exception ex) {
                Log.e(LOGTAG, "Sorting failed...", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ScannedFile> result) {
            m_ListAdapter.setSubText(SORT_BY, m_SortMenuValues.get(ScannedFile.getSortType()).toString());
            if (result != null) {
                if (!m_SearchView && m_ShowIndex == 0) {
                    m_Files = result;
                }
                pageViewHelper.setFiles(result);
            }
        }
    }

    class GalleryClickListener implements OnItemClickListener, OnItemSelectedListener {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            View selected = ((Gallery) arg0).getSelectedView();
            if (arg1.equals(selected)) {
                onClick(goButton);
                onClick(m_CloseBtn);
                if( !m_ArchiveView)
                    NookLibrary.this.onItemClick(lview, null, SHOW_COVERS, -1);
            }
        }

        public void onItemSelected(AdapterView<?> arg0, View arg1, final int position, long arg3) {
            if (m_IconGallery.getVisibility() == View.VISIBLE) {
                pageViewHelper.gotoItem(position + 1);
//                if (m_Toast != null) {
//                    m_Toast.cancel();
//                    m_Toast.getView().setVisibility(View.VISIBLE);
//                }
//                m_Toast = Toast.makeText(NookLibrary.this, pageViewHelper.getCurrent().getTitle(), Toast.LENGTH_SHORT);
//                m_Toast.show();
                m_GalleryTitle.setVisibility(View.VISIBLE);
                m_GalleryTitle.setText(pageViewHelper.getCurrent().getTitle());
                m_Handler.postDelayed(new Runnable() {
                    public void run() {
                        if( position == m_IconGallery.getSelectedItemPosition())
                            m_GalleryTitle.setVisibility(View.INVISIBLE);
                    }
                }, 2000);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }
    }

    public boolean onLongClick(View view) {
        if (view.equals(upButton)) {
            pageViewHelper.gotoTop();
        } else {
            pageViewHelper.gotoBottom();
        }
        return true;
    }
    private void displayAlert() {
        displayAlert(R.string.not_allowed_error);
    }
    private void displayAlert(int error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(error);
        builder.setTitle(R.string.error);
        builder.setNeutralButton(android.R.string.ok, null);
        builder.show();
    }

    public String readDescription(ScannedFile file) {
        String desc =null;
        if (file.matchSubject(getString(R.string.fictionwise))) {
            desc=m_FictionwiseBooks.getDescription(file.getEan());
        } else if (file.matchSubject("B&N")) {
            desc=m_BNBooks.getDescription(file.getEan());
        } else if (file.matchSubject(getString(R.string.smashwords))) {
            desc=m_Smashwords.getDescription(file.getEan());
        } else {
            // local
            desc=m_OtherBooks.getDescription(file.getEan());
        }
        return desc;
    }
    public String getKeywords(ScannedFile file) {
        if (file.matchSubject("B&N")) {
            return m_BNBooks.getKeywordsString(file.getEan());
        }
        String fictionwise = getString(R.string.fictionwise);
        if (file.getBookID() != null) {
            if (file.matchSubject(fictionwise)) {
                return m_FictionwiseBooks.getKeywordsString(file.getEan());
            } else {
                return m_Smashwords.getKeywordsString(file.getEan());
            }
        } else {
            return m_OtherBooks.getKeywordsString(file.getPathName());
        }
    }
    public void updateCover(ScannedFile file) {
        if (file.matchSubject("B&N")) {
            return;
        }
        String fictionwise = getString(R.string.fictionwise);
        if (file.getBookID() != null) {
            if (file.matchSubject(fictionwise)) {
                m_FictionwiseBooks.updateCover(file);
            } else {
                m_Smashwords.updateCover(file);
            }
        } else {
            m_OtherBooks.updateCover(file);
        }
    }
    public void addBookToDB(ScannedFile file) {
        if (file.getBookID() != null) {
            String fictionwise = getString(R.string.fictionwise);
            if (file.matchSubject(fictionwise)) {
                m_FictionwiseBooks.addBookToDB(file);
            } else {
                m_Smashwords.addBookToDB(file);
            }
        } else {
            m_OtherBooks.addBookToDB(file);
        }
    }
    private IBooksService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            try {
                mService = IBooksService.Stub.asInterface(arg1);
                mService.setCallback(mCallback);
                metadataLock.open();
            } catch(Exception ex) {
                Log.e(LOGTAG, "onServiceConnected", ex);
            }

        }

        public void onServiceDisconnected(ComponentName arg0) {
            mService=null;

        }

    };
    private ConditionVariable metadataLock = new ConditionVariable();
    private IBooksServiceCallback.Stub mCallback = new IBooksServiceCallback.Stub() {

        public void getMetaData(final List<BooksData> d, int start, int end) throws RemoteException {
            boolean finished=false;
            Log.e(LOGTAG, "getMetaData -" + start + " " + end);
            final List<ScannedFile> l;
            int size=0;
            synchronized(m_Files) {
                size = m_Files.size();
            }
            if( end != size) {
                final int end1 = end;
                Runnable r = new Runnable() {
                    public void run() {
                        updateMetaData( true, end1);
                    }
                };
                ( new Thread(r)).start();
            } else {
                finished=true;
            }
            synchronized(m_Files) {
                l = new ArrayList<ScannedFile>(m_Files.subList( start, end));
            }
            int i=0;
            for( ScannedFile f:l) {
                if( !f.getBookInDB()) {
                    BooksData b = d.get(i++);
                    f.setTitles( b.titles);
                    f.setDescription(b.description);
                    f.setEan( b.ean);
                    f.setSeries( b.series);
                    for( String key:b.keywords) {
                        f.addKeywords( key);
                    }
                    for( String author: b.contributors) {
                        f.addContributor(author,"");
                    }
                    f.setPublisher( b.publisher);
                    addBookToDB(f);
                }
            }
            if( finished) {
                m_LocalScanDone.open();
            }
        }
    };

    public List<String> getAvailableKeywords() {
        List<String> keywords = new ArrayList<String>(1000);
        for( ScannedFile f: m_Files) {
            if( f.matchSubject("B&N")) {
                m_BNBooks.getKeywords( f.getEan(), keywords);
            } else if( f.getBookID() != null) {
                if( f.matchSubject(getString(R.string.fictionwise))) {
                    m_FictionwiseBooks.getKeywords(f.getEan(), keywords);
                } else {
                    m_Smashwords.getKeywords(f.getEan(), keywords);
                }
            } else {
                m_OtherBooks.getKeywords(f.getPathName(), keywords);
            }
        }
        return keywords;
    }
    public synchronized void updateMetaData(boolean retry, int start) {
        try {
            boolean attempted=false;
            if( mService == null) {
                metadataLock.close();
                bindService(new Intent(BooksService.class.getName()), mConnection, BIND_AUTO_CREATE);
                if( !metadataLock.block(10000) && retry) {
                    if(mService!= null) {
                        try {
                            unbindService(mConnection);
                        } catch(Throwable t2) {
                            mService=null;
                        }
                    }
                    updateMetaData(false, start);
                    return;
                }
            }
            List<String> books = new ArrayList<String>(10);
            int size=0;
            synchronized(m_Files) {
                size=m_Files.size();
            }
            int end=start;
            int total=0;
            while( total < 10 && end < size) {
                ScannedFile f = m_Files.get(end);
                if( !f.getBookInDB()) {
                    total++;
                    attempted=true;
                    books.add(f.getPathName());
                    if( total == 10) {
                        mService.setData(books, start, end);
                        books.clear();
                        break;
                    }
                }
                end++;
            }
            if( books.size() >0) {
                mService.setData(books, start, end);
            }
            if( !attempted && start ==0) {
                m_LocalScanDone.open();
            }
        } catch(Throwable t) {
            try {
                if( mService != null)
                    unbindService(mConnection);
            } catch(Throwable t1) {
                mService=null;
            }
            if( retry) {
                updateMetaData(false, start);
                return;
            }
        }
    }
}
