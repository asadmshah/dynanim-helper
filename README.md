# DynanimHelper

A library that helps you group SpringAnimations in the newly released dynamic-animations support
library. The newly released `dynamic-animations` library is pretty cool and all but you have to manage
animations of multiple properties by yourself. This library helps solve that.

I should've named this library `SpringAnimationGroup`, not sure why I didn't.

# Download

```
repositories {
	maven { url = 'https://jitpack.io' }
}

dependencies {
	compile 'com.github.asadmshah:dynanim-helper:0.1.0'
}
```

# Usage

### SpringAnimation

```java
SpringForce sfx = new SpringForce(100f);
sfx.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
sfx.setStiffness(SpringForce.STIFFNESS_MEDIUM);
SpringAnimation sax = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 100f);
sax.setSpring(sfx);

SpringForce sfy = new SpringForce(200f);
sfx.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
sfx.setStiffness(SpringForce.STIFFNESS_MEDIUM);
SpringAnimation say = new SpringAnimation(view, DynamicAnimation.TRANSLATION_Y, 200f);
say.setSpring(sfy);

sax.start();
say.start();
```

### SpringAnimationGroup

```java
SpringAnimationGroup
    .from(view)
    .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
    .setStiffness(SpringForce.STIFFNESS_MEDIUM);
    .setFinalPosition(DynamicAnimation.TRANSLATION_X, 100f)
    .setFinalPosition(DynamicAnimation.TRANSLATION_Y, 200f)
    .build()
    .start();
```

# License
Copyright (C) 2017 Asad Shah

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.