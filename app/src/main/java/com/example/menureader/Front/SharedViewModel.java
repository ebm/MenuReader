package com.example.menureader.Front;

import android.graphics.Bitmap;
import androidx.lifecycle.ViewModel;
import com.example.menureader.Handling.LocalCache;
import com.example.menureader.Handling.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Facilitates sharing data between fragments
 */
public class SharedViewModel extends ViewModel {
    private Bitmap bitmap;
    private Menu menu;
    private final List<Menu> menuList;
    private final LocalCache cache;
    private OnMenuAddedListener menuAddedListener;

    public interface OnMenuAddedListener {
        void onMenuAdded(int index);
    }

    public SharedViewModel() {
        menuList = new ArrayList<>();
        cache = new LocalCache();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void addMenu(Menu menu) {
        menuList.add(menu);
        if (menuAddedListener != null) {
            menuAddedListener.onMenuAdded(menuList.size() - 1);
        }
    }

    public List<Menu> getMenuList() {
        return menuList;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public LocalCache getCache() {
        return cache;
    }

    public void setMenuAddedListener(OnMenuAddedListener listener) {
        this.menuAddedListener = listener;
    }

    public void clearMenuAddedListener() {
        this.menuAddedListener = null;
    }
}
