package com.bignerdranch.android.sunset;

import android.animation.Animator;
import android.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for allowing collective start, reversals, and animation ending event listeners on a set
 * of animator objects. This is made over the AnimatorSet class, as the reverse functionality is not
 * available until API 26.
 */
public class Scene {


    private List<ObjectAnimator> mAnimatorList = new ArrayList<>();

    public interface SceneFinishedListener {
        void onSceneFinished();
    }

    public void setSceneFinishedListener(final SceneFinishedListener listener) {
        for(ObjectAnimator animator : mAnimatorList) {
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    // Sets listener on the first animator, this is fine as long as each animator
                    // lasts the same amount of time (ideal for our uses, not general case)
                    listener.onSceneFinished();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            break;
        }
    }

    public void removeSceneFinishedListeners() {
        for(ObjectAnimator animator : mAnimatorList){
            animator.removeAllListeners();
        }
    }

    public void add(ObjectAnimator animator){
        mAnimatorList.add(animator);
    }

    public void start(){
        for(ObjectAnimator animator : mAnimatorList) animator.start();
    }

    public void reverse() {
        for(ObjectAnimator animator : mAnimatorList) animator.reverse();
    }

    public boolean isRunning() {
        for(ObjectAnimator animator : mAnimatorList){
            if(animator.isRunning()) return true;
        }
        return false;
    }

}
