package com.sobremesa.waywt.managers;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpan extends MetricAffectingSpan {

  public TypefaceSpan(Context context) {
  }

  @Override
  public void updateMeasureState(TextPaint p) {
      p.setTypeface(FontManager.INSTANCE.getAppFont());

      // Note: This flag is required for proper typeface rendering
      p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
  }

  @Override
  public void updateDrawState(TextPaint tp) {
      tp.setTypeface(FontManager.INSTANCE.getAppFont());

      // Note: This flag is required for proper typeface rendering
      tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
  }
}