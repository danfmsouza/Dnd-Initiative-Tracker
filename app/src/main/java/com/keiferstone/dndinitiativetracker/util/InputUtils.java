package com.keiferstone.dndinitiativetracker.util;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputUtils {
    public static void showKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
            imm.showSoftInput(view, 0);
        }
    }

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
