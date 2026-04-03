package com.example.menureader;

import android.graphics.Bitmap;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private Bitmap bitmap;
    private Menu menu;
    private final List<Menu> menuList;

    public SharedViewModel() {
        menuList = new ArrayList<>();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void addMenu(Menu menu) {
        menuList.add(menu);
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
}
