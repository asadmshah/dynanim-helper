package com.asadmshah.dynanimhelper;

import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for grouping multiple {@link DynamicAnimation} into one group.
 */
public class SpringAnimationGroup {

    /**
     *    Holds references to the actual OnAnimationEndListener in order to be removed in the call to removeEndListener.
     *
     *    @see #addEndListener(OnGroupAnimationEndListener)
     *    @see #removeEndListener(OnGroupAnimationEndListener)
     */
    private final Map<OnGroupAnimationEndListener, DynamicAnimation.OnAnimationEndListener> animationGroupEndListeners;

    /**
     *    Holds references to the actual OnAnimationEndListener in order to be removed in the call to removeEndListener.
     *
     *    @see #addEndListener(DynamicAnimation.ViewProperty, OnPropertyAnimationEndListener)
     *    @see #removeEndListener(DynamicAnimation.ViewProperty, OnPropertyAnimationEndListener)
     */
    private final Map<OnPropertyAnimationEndListener, DynamicAnimation.OnAnimationEndListener> animationPropertyEndListeners;

    /**
     * Holds references to the actual OnAnimationUpdateListener in order to be removed in the call to removeEndListener.
     *
     * @see #addUpdateListener(DynamicAnimation.ViewProperty, OnPropertyAnimationUpdateListener)
     * @see #removeUpdateListener(DynamicAnimation.ViewProperty, OnPropertyAnimationUpdateListener)
     */
    private final Map<OnPropertyAnimationUpdateListener, DynamicAnimation.OnAnimationUpdateListener> animationPropertyUpdateListeners;

    /**
     * Holds all of the {@link SpringAnimation} for each respective {@link DynamicAnimation.ViewProperty}.
     */
    private final Map<DynamicAnimation.ViewProperty, SpringAnimation> animationsMap;

    /**
     * Creates the group for the given animations.
     *
     * @param animationsMap mapping of {@link DynamicAnimation.ViewProperty} to {@link SpringAnimation}.
     */
    private SpringAnimationGroup(Map<DynamicAnimation.ViewProperty, SpringAnimation> animationsMap) {
        animationGroupEndListeners = new HashMap<>();
        animationPropertyEndListeners = new HashMap<>();
        animationPropertyUpdateListeners = new HashMap<>();

        this.animationsMap = animationsMap;
    }

    /**
     * Adds an end listener to the animation group for receiving onAnimationEnd callbacks. If the listener
     * is {@code null} or has already been added to the list of listeners for the animation, no op.
     *
     * @see DynamicAnimation#addEndListener(DynamicAnimation.OnAnimationEndListener)
     *
     * @param listener the listener to be added
     * @return the animation group to which the listener is added
     */
    public SpringAnimationGroup addEndListener(@NonNull final OnGroupAnimationEndListener listener) {
        if (!animationsMap.isEmpty()) {
            DynamicAnimation.OnAnimationEndListener pureListener = new DynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                    listener.onGroupAnimationEnd(SpringAnimationGroup.this, canceled, value, velocity);
                }
            };

            animationGroupEndListeners.put(listener, pureListener);

            SpringAnimation animation = getFirstSpringAnimation();
            if (animation != null) {
                animation.addEndListener(pureListener);
            }
        }

        return this;
    }

    /**
     * Adds an end listener to the animation group for receiving onAnimationEnd callbacks for the
     * given view property. If the listener is {@code null} or has already been added to the list of
     * listeners for the animation, no op.
     *
     * @see DynamicAnimation#addEndListener(DynamicAnimation.OnAnimationEndListener)
     *
     * @param property the view property in question
     * @param listener the listener to be added
     * @return the animation group to which the listener is added
     */
    public SpringAnimationGroup addEndListener(@NonNull final DynamicAnimation.ViewProperty property, @NonNull final OnPropertyAnimationEndListener listener) {
        if (animationsMap.containsKey(property)) {
            DynamicAnimation.OnAnimationEndListener pureListener = new DynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                    listener.onPropertyAnimationEnd(SpringAnimationGroup.this, property, canceled, value, velocity);
                }
            };

            animationPropertyEndListeners.put(listener, pureListener);

            animationsMap.get(property).addEndListener(pureListener);
        }

        return this;
    }

    /**
     * Adds an update listener to the animation for the given view property.
     *
     * @see DynamicAnimation#addUpdateListener(DynamicAnimation.OnAnimationUpdateListener)
     *
     * @param property the view property in question
     * @param listener the listener to be added
     * @return the animation to which the listener is added
     * @throws UnsupportedOperationException if the update listener is added after the animation has
     *                                       started
     */
    public SpringAnimationGroup addUpdateListener(@NonNull final DynamicAnimation.ViewProperty property, @NonNull final OnPropertyAnimationUpdateListener listener) {
        if (animationsMap.containsKey(property)) {
            DynamicAnimation.OnAnimationUpdateListener pureListener = new DynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                    listener.onPropertyAnimationUpdate(SpringAnimationGroup.this, property, value, velocity);
                }
            };

            animationPropertyUpdateListeners.put(listener, pureListener);

            animationsMap.get(property).addUpdateListener(pureListener);
        }

        return this;
    }

    /**
     * Updates the final position of the spring for the given view property.
     *
     * @see SpringAnimation#animateToFinalPosition(float)
     *
     * @param property the view property in question
     * @param finalPosition rest position of the spring
     */
    public void animateToFinalPosition(@NonNull DynamicAnimation.ViewProperty property, float finalPosition) {
        if (animationsMap.containsKey(property)) {
            animationsMap.get(property).animateToFinalPosition(finalPosition);
        }
    }

    /**
     * Cancels the on-going animation for the given view property
     *
     * @see SpringAnimation#cancel()
     *
     * @throws AndroidRuntimeException if this method is not called on the main thread
     */
    public void cancel() {
        for (SpringAnimation animation : animationsMap.values()) {
            animation.cancel();
        }
    }

    /**
     * Queries whether the spring in the given view property can eventually come to the rest position.
     *
     * @see SpringAnimation#canSkipToEnd()
     *
     * @param property the view property in question
     * @return {@code true} if the spring is damped, otherwise {@code false}
     */
    public boolean canSkipToEnd(@NonNull DynamicAnimation.ViewProperty property) {
        return animationsMap.containsKey(property) && animationsMap.get(property).canSkipToEnd();
    }

    /**
     * Returns the spring that the animation for the given view property uses for animations.
     *
     * @see SpringAnimation#getSpring()
     *
     * @param property the view property in question
     * @return the spring in use
     */
    @Nullable
    public SpringForce getSpring(@NonNull DynamicAnimation.ViewProperty property) {
        if (animationsMap.containsKey(property)) {
            return animationsMap.get(property).getSpring();
        }
        return null;
    }

    /**
     * Returns whether the animation is currently running.
     *
     * @return {@code true} if the animation is currently running, {@code false} otherwise
     */
    public boolean isRunning() {
        SpringAnimation animation = getFirstSpringAnimation();
        return animation != null && animation.isRunning();
    }

    /**
     * Removes the end listener from the animation group, so as to stop receiving animation end callbacks.
     *
     * @param listener the listener to be removed
     */
    public void removeEndListener(@NonNull OnGroupAnimationEndListener listener) {
        if (animationGroupEndListeners.containsKey(listener)) {
            SpringAnimation animation = getFirstSpringAnimation();
            if (animation != null) {
                animation.removeEndListener(animationGroupEndListeners.get(listener));
            }
            animationGroupEndListeners.remove(listener);
        }
    }

    /**
     * Removes the end listener from the animation for the given view property, so as to stop
     * receiving animation end callbacks.
     *
     * @param property the view property in question
     * @param listener the listener to be removed
     */
    public void removeEndListener(@NonNull DynamicAnimation.ViewProperty property, @NonNull OnPropertyAnimationEndListener listener) {
        if (animationPropertyEndListeners.containsKey(listener)) {
            SpringAnimation animation = animationsMap.get(property);
            if (animation != null) {
                animation.removeEndListener(animationPropertyEndListeners.get(listener));
            }
            animationPropertyEndListeners.remove(listener);
        }
    }

    /**
     * Removes the update listener from the animation for the given view property, so as to stop
     * receiving animation update callbacks.
     *
     * @param property the view property in question
     * @param listener the listener to be removed
     */
    public void removeUpdateListener(@NonNull DynamicAnimation.ViewProperty property, @NonNull OnPropertyAnimationUpdateListener listener) {
        if (animationPropertyUpdateListeners.containsKey(listener)) {
            SpringAnimation animation = animationsMap.get(property);
            if (animation != null) {
                animation.removeUpdateListener(animationPropertyUpdateListeners.get(listener));
            }
            animationPropertyUpdateListeners.remove(listener);
        }
    }

    /**
     * Uses the given spring for the given view property as the force that drives this animation.
     *
     * @see SpringAnimation#setSpring(SpringForce)
     *
     * @param property the view property in question
     * @param springForce a pre-defined spring force that drives the animation
     * @return the animation that the spring force is set on
     */
    public SpringAnimationGroup setSpring(@NonNull DynamicAnimation.ViewProperty property, @NonNull SpringForce springForce) {
        if (animationsMap.containsKey(property)) {
            animationsMap.get(property).setSpring(springForce);
        }

        return this;
    }

    /**
     * Skips to the end of the animation.
     *
     * @see SpringAnimation#skipToEnd()
     *
     * @throws IllegalStateException if the spring is undamped (i.e. damping ratio = 0)
     * @throws AndroidRuntimeException if this method is not called on the main thread
     */
    public void skipToEnd() {
        for (SpringAnimation animation : animationsMap.values()) {
            animation.skipToEnd();
        }
    }

    /**
     * Skips to the end of the animation for the given view property.
     *
     * @see SpringAnimation#skipToEnd()
     *
     * @param property the view property in question
     * @throws IllegalStateException if the spring is undamped (i.e. damping ratio = 0)
     * @throws AndroidRuntimeException if this method is not called on the main thread
     */
    public void skipToEnd(@NonNull DynamicAnimation.ViewProperty property) {
        if (animationsMap.containsKey(property)) {
            animationsMap.get(property).skipToEnd();
        }
    }

    /**
     * Starts all the animations in the group.
     *
     * @see SpringAnimation#start()
     *
     * @throws AndroidRuntimeException if this method is not called on the main thread
     */
    public void start() {
        for (SpringAnimation animation : animationsMap.values()) {
            animation.start();
        }
    }

    @Nullable
    private SpringAnimation getFirstSpringAnimation() {
        for (SpringAnimation animation : animationsMap.values()) {
            return animation;
        }
        return null;
    }

    /**
     * Creates a {@link Builder} for the given view.
     *
     * @param view to animate.
     * @return the newly generated builder.
     */
    public static Builder from(@NonNull View view) {
        return new Builder(view);
    }

    /**
     * Builder class for generating a {@link SpringAnimationGroup}
     */
    public static class Builder {

        /**
         * Holds the {@link SpringForce} for the the {@link DynamicAnimation.ViewProperty}
         */
        private final Map<DynamicAnimation.ViewProperty, SpringForce> propertiesMap = new HashMap<>();

        /**
         * Holds the max value for the {@link DynamicAnimation.ViewProperty}
         */
        private final Map<DynamicAnimation.ViewProperty, Float> maxValuesMap = new HashMap<>();

        /**
         * Holds the min value for the {@link DynamicAnimation.ViewProperty}
         */
        private final Map<DynamicAnimation.ViewProperty, Float> minValuesMap = new HashMap<>();

        /**
         * Holds the start value for the {@link DynamicAnimation.ViewProperty}
         */
        private final Map<DynamicAnimation.ViewProperty, Float> startValuesMap = new HashMap<>();

        /**
         * Holds the velocities value for the {@link DynamicAnimation.ViewProperty}
         */
        private final Map<DynamicAnimation.ViewProperty, Float> startVelocitiesMap = new HashMap<>();

        /**
         * The view to pass to animate.
         */
        private final View view;

        /**
         * The damping ratio to be applied to {@link SpringForce}. This value is only applied to
         * properties where the value wasn't explicitly given using {@link #setSpring(DynamicAnimation.ViewProperty, SpringForce)}
         * or {@link #setDampingRatio(DynamicAnimation.ViewProperty, float)}.
         */
        private float dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;

        /**
         * The stiffness to be applied to {@link SpringForce}. This value is only applied to
         * properties where the value wasn't explicitly given using {@link #setSpring(DynamicAnimation.ViewProperty, SpringForce)}
         * or {@link #setStiffness(DynamicAnimation.ViewProperty, float)}.
         */
        private float stiffness = SpringForce.STIFFNESS_MEDIUM;

        private Builder(View view) {
            this.view = view;
        }

        /**
         * Sets the {@link SpringForce} for the given {@link DynamicAnimation.ViewProperty}.
         *
         * @param property to apply the spring force to
         * @param springForce to use on the view
         * @return this builder
         */
        public Builder setSpring(@NonNull DynamicAnimation.ViewProperty property, @NonNull SpringForce springForce) {
            propertiesMap.put(property, springForce);

            return this;
        }

        /**
         * Sets the rest position of the spring for the given {@link DynamicAnimation.ViewProperty}.
         *
         * @param property to apply the spring force to
         * @param finalPosition rest position of the spring
         * @return this builder
         */
        public Builder setFinalPosition(@NonNull DynamicAnimation.ViewProperty property, float finalPosition) {
            SpringForce springForce = initIfNew(property);
            springForce.setFinalPosition(finalPosition);

            return this;
        }

        /**
         * Sets the default damping ratio to apply to any previous and future registered view properties.
         * This value can be overridden for the specific view property using the method
         * {@link #setDampingRatio(DynamicAnimation.ViewProperty, float)}
         *
         * @see SpringForce#setDampingRatio(float)
         *
         * @param dampingRatio damping ratio of the spring, it should be non-negative
         * @return this builder
         */
        public Builder setDampingRatio(@FloatRange(from = 0.0) float dampingRatio) {
            this.dampingRatio = dampingRatio;

            for (SpringForce force : propertiesMap.values()) {
                force.setDampingRatio(dampingRatio);
            }

            return this;
        }

        /**
         * Sets damping ratio for the given view property.
         *
         * @see SpringForce#setDampingRatio(float)
         *
         * @param property to apply the value to
         * @param dampingRatio damping ratio of the spring, it should be non-negative
         * @return this builder
         */
        public Builder setDampingRatio(@NonNull DynamicAnimation.ViewProperty property, @FloatRange(from = 0.0) float dampingRatio) {
            SpringForce springForce = initIfNew(property);
            springForce.setDampingRatio(dampingRatio);

            return this;
        }

        /**
         * Sets the default stiffness to apply to any previous and future registered view properties.
         * This value can be overridden for the specific view property using the method
         * {@link #setStiffness(DynamicAnimation.ViewProperty, float)}
         *
         * @see SpringForce#setStiffness(float)
         *
         * @param stiffness non-negative stiffness constant of a spring
         * @return this builder
         */
        public Builder setStiffness(float stiffness) {
            this.stiffness = stiffness;

            for (SpringForce force : propertiesMap.values()) {
                force.setStiffness(stiffness);
            }

            return this;
        }

        /**
         * Sets stiffness for the given view property.
         *
         * @see SpringForce#setStiffness(float)
         *
         * @param property to apply the value to
         * @param stiffness non-negative stiffness constant of a spring
         * @return this builder
         */
        public Builder setStiffness(@NonNull DynamicAnimation.ViewProperty property, float stiffness) {
            SpringForce springForce = initIfNew(property);
            springForce.setStiffness(stiffness);

            return this;
        }

        /**
         * Sets the max value of the animation for the given view property.
         *
         * @see DynamicAnimation#setMaxValue(float)
         *
         * @param property to apply the value to
         * @param max maximum value of the property to be animated
         * @return this builder
         */
        public Builder setMaxValue(@NonNull DynamicAnimation.ViewProperty property, float max) {
            maxValuesMap.put(property, max);

            return this;
        }

        /**
         * Sets the min value of the animation for the given view property.
         *
         * @see DynamicAnimation#setMinValue(float)
         *
         * @param property to apply the value to
         * @param min minimum value of the property to be animated
         * @return this builder
         */
        public Builder setMinValue(@NonNull DynamicAnimation.ViewProperty property, float min) {
            minValuesMap.put(property, min);

            return this;
        }

        /**
         * Sets the start value of the animation for the given view property.
         *
         * @see DynamicAnimation#setStartValue(float)
         *
         * @param property to apply the value to
         * @param startValue start value for the animation
         * @return this builder
         */
        public Builder setStartValue(@NonNull DynamicAnimation.ViewProperty property, float startValue) {
            startValuesMap.put(property, startValue);

            return this;
        }

        /**
         * Sets the start velocity of the animation for the given view property.
         *
         * @see DynamicAnimation#setStartVelocity(float)
         *
         * @param property to apply the value to
         * @param startVelocity start velocity of the animation in pixel/second
         * @return this builder
         */
        public Builder setStartVelocity(@NonNull DynamicAnimation.ViewProperty property, float startVelocity) {
            startVelocitiesMap.put(property, startVelocity);

            return this;
        }

        private SpringForce initIfNew(@NonNull DynamicAnimation.ViewProperty property) {
            SpringForce springForce = propertiesMap.get(property);
            if (springForce == null) {
                springForce = new SpringForce();
                springForce.setDampingRatio(dampingRatio);
                springForce.setStiffness(stiffness);
                propertiesMap.put(property, springForce);
            }

            return springForce;
        }

        /**
         * Builds a {@link SpringAnimationGroup} the properties set through this builder.
         *
         * @return the SpringAnimationGroup
         */
        public SpringAnimationGroup build() {
            Map<DynamicAnimation.ViewProperty, SpringAnimation> map = new HashMap<>();
            for (Map.Entry<DynamicAnimation.ViewProperty, SpringForce> entry : propertiesMap.entrySet()) {
                SpringAnimation animation = new SpringAnimation(view, entry.getKey(), entry.getValue().getFinalPosition());
                animation.setSpring(entry.getValue());
                map.put(entry.getKey(), animation);

                if (maxValuesMap.containsKey(entry.getKey())) animation.setMaxValue(maxValuesMap.get(entry.getKey()));
                if (minValuesMap.containsKey(entry.getKey())) animation.setMinValue(minValuesMap.get(entry.getKey()));
                if (startValuesMap.containsKey(entry.getKey())) animation.setStartValue(startValuesMap.get(entry.getKey()));
                if (startVelocitiesMap.containsKey(entry.getKey())) animation.setStartVelocity(startVelocitiesMap.get(entry.getKey()));
            }
            return new SpringAnimationGroup(map);
        }

    }

    /**
     * An animation listener that receives end notifications from an animation group.
     *
     * @see android.support.animation.DynamicAnimation.OnAnimationEndListener
     */
    public interface OnGroupAnimationEndListener {
        void onGroupAnimationEnd(SpringAnimationGroup group, boolean canceled, float value, float velocity);
    }

    /**
     * An animation listener that receives end notifications from an animation.
     *
     * @see android.support.animation.DynamicAnimation.OnAnimationEndListener
     */
    public interface OnPropertyAnimationEndListener {
        void onPropertyAnimationEnd(SpringAnimationGroup group, DynamicAnimation.ViewProperty property, boolean canceled, float value, float velocity);
    }

    /**
     * @see android.support.animation.DynamicAnimation.OnAnimationUpdateListener
     */
    public interface OnPropertyAnimationUpdateListener {
        void onPropertyAnimationUpdate(SpringAnimationGroup group, DynamicAnimation.ViewProperty property, float value, float velocity);
    }
}
