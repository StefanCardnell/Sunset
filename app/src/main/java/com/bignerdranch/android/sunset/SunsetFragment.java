package com.bignerdranch.android.sunset;


import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class SunsetFragment extends Fragment {

    private static final String TAG = "SunsetFragment";

    private static final String STATE_SUNRISE = "sunrise";
    private static final String STATE_SUNSET = "sunset";
    private static final String STATE_UNSET = "unset";

    private View mSceneView;
    private View mSunView;
    private View mSunReflectView;
    private View mSkyView;
    private List<View> mRayViews = new ArrayList<>();

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    private long SUNSET_TIME = 3000;
    private long NIGHTSKY_TIME = 1500;

    private Scene mSunMovingScene = new Scene();
    private Scene mNightSkyScene = new Scene();

    private String mAnimationState = STATE_UNSET;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSunReflectView = view.findViewById(R.id.sun_reflection);
        mSkyView = view.findViewById(R.id.sky);

        int[] rayIds = {R.id.ray1, R.id.ray2, R.id.ray3, R.id.ray4};
        for(int rayId : rayIds){
            View v = view.findViewById(rayId);
            setRotationAnimation(v);
            mRayViews.add(v);
        }

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        setPulseAnimation(mSunView);
        setPulseAnimation(mSunReflectView);

        mSunReflectView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Set reflection to the same Y as the sun after the layout
                mSunReflectView.setVisibility(View.VISIBLE);
                mSunReflectView.setY(mSunView.getTop());
            }
        });

        mSceneView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (mAnimationState) {
                    case STATE_UNSET:
                        startAnimation();
                        break;
                    case STATE_SUNRISE:
                        sunriseToSunset();
                        break;
                    case STATE_SUNSET:
                        sunsetToSunrise();
                        break;
                }
            }
        });

        return view;

    }

    private void setRotationAnimation(View view){

        float rotation = view.getRotation();
        float rotationEnd = rotation + 360;

        ObjectAnimator rotateAnimator = ObjectAnimator
                .ofFloat(view, "rotation", rotation, rotationEnd)
                .setDuration(60000);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);

        rotateAnimator.start();
    }

    private void setPulseAnimation(View view) {
        float scaleX = view.getScaleX();
        float scaleY = view.getScaleY();

        float scaleXEnd = scaleX * 1.2f;
        float scaleYEnd = scaleY * 1.2f;

        ObjectAnimator pulseAnimatorX = ObjectAnimator
                .ofFloat(view, "scaleX", scaleX, scaleXEnd)
                .setDuration(3000);
        ObjectAnimator pulseAnimatorY = ObjectAnimator
                .ofFloat(view, "scaleY", scaleY, scaleYEnd)
                .setDuration(3000);

        pulseAnimatorX.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimatorX.setRepeatMode(ValueAnimator.REVERSE);

        pulseAnimatorY.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimatorY.setRepeatMode(ValueAnimator.REVERSE);

        pulseAnimatorX.start();
        pulseAnimatorY.start();

    }

    private void startAnimation() {

        float sunYStart = mSunView.getTranslationY();
        float sunYEnd = (mSkyView.getHeight() + 100) - mSunView.getTop(); // Extra 100 to deal with pulsating

        ObjectAnimator sunHeightAnimator = ObjectAnimator
                .ofFloat(mSunView, "translationY", sunYStart, sunYEnd)
                .setDuration(SUNSET_TIME);
        sunHeightAnimator.setInterpolator(new AccelerateInterpolator());
        mSunMovingScene.add(sunHeightAnimator);

        for(View rayView: mRayViews) {
            ObjectAnimator rayAnimator = ObjectAnimator
                    .ofFloat(rayView, "translationY", sunYStart, sunYEnd)
                    .setDuration(SUNSET_TIME);
            rayAnimator.setInterpolator(new AccelerateInterpolator());
            mSunMovingScene.add(rayAnimator);
        }

        float sunReflectYStart = mSunReflectView.getTranslationY();
        float sunReflectYEnd =  (-mSunReflectView.getTop()) - 100 - mSunReflectView.getHeight();

        ObjectAnimator sunReflectHeightAnimator = ObjectAnimator
                .ofFloat(mSunReflectView, "translationY", sunReflectYStart, sunReflectYEnd)
                .setDuration(SUNSET_TIME);
        sunReflectHeightAnimator.setInterpolator(new AccelerateInterpolator());
        mSunMovingScene.add(sunReflectHeightAnimator);

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor)
                .setDuration(SUNSET_TIME);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
        mSunMovingScene.add(sunsetSkyAnimator);

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor)
                .setDuration(NIGHTSKY_TIME);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());
        mNightSkyScene.add(nightSkyAnimator);

        addSunsetListeners();
        mSunMovingScene.start();
        mAnimationState = STATE_SUNSET;
    }

    /**
     * Reverses animation from a sunrise to a sunset.
     */
    private void sunriseToSunset() {
        addSunsetListeners();
        // 1) Night sky is changing. In this case reverse it and nothing else.
        if(mNightSkyScene.isRunning()) {
            mNightSkyScene.reverse();
        }
        // 2) Sun was moving up, reverse it.
        else if(mSunMovingScene.isRunning()) {
            mSunMovingScene.reverse();
        }
        // 3) Sunrise had finished, start sunset again. We need to call start and not reverse, as 
        // reverse on a finished animation would make the sun start from the bottom and go back to 
        // the top.
        else {
            mSunMovingScene.start();
        }
        mAnimationState = STATE_SUNSET;
    }

    /**
     * Reverses animation from a sunset to a sunrise.
     */
    private void sunsetToSunrise() {
        addSunriseListeners();
        // 1) Sun was still setting. In this case reverse it and nothing else
        if(mSunMovingScene.isRunning()){
            mSunMovingScene.reverse();
            // 2) Night Sky was setting or had set already, reverse it.
        } else {
            mNightSkyScene.reverse();
        }
        mAnimationState = STATE_SUNRISE;
    }
    /**
     * Removes current animation listeners and adds those needed for a sunset.
     */
    void addSunsetListeners() {
        mNightSkyScene.removeSceneFinishedListeners();
        mSunMovingScene.removeSceneFinishedListeners();
        mSunMovingScene.setSceneFinishedListener(new Scene.SceneFinishedListener() {
            @Override
            public void onSceneFinished() {
                mNightSkyScene.start();
            }
        });
    }

    /**
     * Removes current animation listeners and adds those needed for a sunrise.
     */
    private void addSunriseListeners() {
        mSunMovingScene.removeSceneFinishedListeners();
        mNightSkyScene.removeSceneFinishedListeners();
        mNightSkyScene.setSceneFinishedListener(new Scene.SceneFinishedListener(){
            @Override
            public void onSceneFinished() {
                mSunMovingScene.reverse();
            }
        });
    }

}