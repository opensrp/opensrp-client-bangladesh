package org.smartregister.growplus.activity.mocks;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.AnyRes;
import android.support.annotation.ArrayRes;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.FractionRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.mockito.Mockito;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kaderchowdhury on 03/12/17.
 */

public class MockResources extends Resources {
    public MockResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(Mockito.mock(AssetManager.class), Mockito.mock(DisplayMetrics.class), Mockito.mock(Configuration.class));
    }

    @NonNull
    @Override
    public CharSequence getText(@StringRes int id) throws NotFoundException {
        return super.getText(id);
    }

    @NonNull
    @Override
    public CharSequence getQuantityText(@PluralsRes int id, int quantity) throws NotFoundException {
        return super.getQuantityText(id, quantity);
    }

    @NonNull
    @Override
    public String getString(@StringRes int id) throws NotFoundException {
        return super.getString(id);
    }

    @NonNull
    @Override
    public String getString(@StringRes int id, Object... formatArgs) throws NotFoundException {
        return super.getString(id, formatArgs);
    }

    @NonNull
    @Override
    public String getQuantityString(@PluralsRes int id, int quantity, Object... formatArgs) throws NotFoundException {
        return super.getQuantityString(id, quantity, formatArgs);
    }

    @NonNull
    @Override
    public String getQuantityString(@PluralsRes int id, int quantity) throws NotFoundException {
        return super.getQuantityString(id, quantity);
    }

    @Override
    public CharSequence getText(@StringRes int id, CharSequence def) {
        return super.getText(id, def);
    }

    @NonNull
    @Override
    public CharSequence[] getTextArray(@ArrayRes int id) throws NotFoundException {
        return super.getTextArray(id);
    }

    @NonNull
    @Override
    public String[] getStringArray(@ArrayRes int id) throws NotFoundException {
        return super.getStringArray(id);
    }

    @NonNull
    @Override
    public int[] getIntArray(@ArrayRes int id) throws NotFoundException {
        return super.getIntArray(id);
    }

    @NonNull
    @Override
    public TypedArray obtainTypedArray(@ArrayRes int id) throws NotFoundException {
        return super.obtainTypedArray(id);
    }

    @Override
    public float getDimension(@DimenRes int id) throws NotFoundException {
        return super.getDimension(id);
    }

    @Override
    public int getDimensionPixelOffset(@DimenRes int id) throws NotFoundException {
        return super.getDimensionPixelOffset(id);
    }

    @Override
    public int getDimensionPixelSize(@DimenRes int id) throws NotFoundException {
        return super.getDimensionPixelSize(id);
    }

    @Override
    public float getFraction(@FractionRes int id, int base, int pbase) {
        return super.getFraction(id, base, pbase);
    }

    @Override
    public Drawable getDrawable(@DrawableRes int id) throws NotFoundException {
        return super.getDrawable(id);
    }

    @Override
    public Drawable getDrawable(@DrawableRes int id, @Nullable Theme theme) throws NotFoundException {
        return super.getDrawable(id, theme);
    }

    @Override
    public Drawable getDrawableForDensity(@DrawableRes int id, int density) throws NotFoundException {
        return super.getDrawableForDensity(id, density);
    }

    @Override
    public Drawable getDrawableForDensity(@DrawableRes int id, int density, @Nullable Theme theme) {
        return super.getDrawableForDensity(id, density, theme);
    }

    @Override
    public Movie getMovie(@RawRes int id) throws NotFoundException {
        return super.getMovie(id);
    }

    @Override
    public int getColor(@ColorRes int id) throws NotFoundException {
        return super.getColor(id);
    }

    @Nullable
    @Override
    public ColorStateList getColorStateList(@ColorRes int id) throws NotFoundException {
        return super.getColorStateList(id);
    }

    @Override
    public boolean getBoolean(@BoolRes int id) throws NotFoundException {
        return super.getBoolean(id);
    }

    @Override
    public int getInteger(@IntegerRes int id) throws NotFoundException {
        return super.getInteger(id);
    }

    @Override
    public XmlResourceParser getLayout(@LayoutRes int id) throws NotFoundException {
        return super.getLayout(id);
    }

    @Override
    public XmlResourceParser getAnimation(@AnimRes int id) throws NotFoundException {
        return super.getAnimation(id);
    }

    @Override
    public XmlResourceParser getXml(@XmlRes int id) throws NotFoundException {
        return super.getXml(id);
    }

    @Override
    public InputStream openRawResource(@RawRes int id) throws NotFoundException {
        return super.openRawResource(id);
    }

    @Override
    public InputStream openRawResource(@RawRes int id, TypedValue value) throws NotFoundException {
        return super.openRawResource(id, value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(@RawRes int id) throws NotFoundException {
        return super.openRawResourceFd(id);
    }

    @Override
    public void getValue(@AnyRes int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        super.getValue(id, outValue, resolveRefs);
    }

    @Override
    public void getValueForDensity(@AnyRes int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        super.getValueForDensity(id, density, outValue, resolveRefs);
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        super.getValue(name, outValue, resolveRefs);
    }

    @Override
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return super.obtainAttributes(set, attrs);
    }

    @Override
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
//        super.updateConfiguration(config, metrics);
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return super.getDisplayMetrics();
    }

    @Override
    public Configuration getConfiguration() {
        return super.getConfiguration();
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        return super.getIdentifier(name, defType, defPackage);
    }

    @Override
    public String getResourceName(@AnyRes int resid) throws NotFoundException {
        return super.getResourceName(resid);
    }

    @Override
    public String getResourcePackageName(@AnyRes int resid) throws NotFoundException {
        return super.getResourcePackageName(resid);
    }

    @Override
    public String getResourceTypeName(@AnyRes int resid) throws NotFoundException {
        return super.getResourceTypeName(resid);
    }

    @Override
    public String getResourceEntryName(@AnyRes int resid) throws NotFoundException {
        return super.getResourceEntryName(resid);
    }

    @Override
    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle) throws XmlPullParserException, IOException {
        super.parseBundleExtras(parser, outBundle);
    }

    @Override
    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle) throws XmlPullParserException {
        super.parseBundleExtra(tagName, attrs, outBundle);
    }
}
