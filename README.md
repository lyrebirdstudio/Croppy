# Croppy
<img src="https://raw.githubusercontent.com/lyrebirdstudio/Croppy/master/art/art.png"/>

# Basic Usage
```kotlin
//Start croppy (source uri is the original image.)
val cropRequest = CropRequest.Auto(sourceUri = uri, requestCode = 101)
Croppy.start(this, cropRequest)
```
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == 101) {
           imageview.setImageURI(data.data)
     }
}
```

# Demo
<img src="https://github.com/lyrebirdstudio/Croppy/blob/master/art/artgif.gif?raw=true"/>

# Features

### Double tap focus 👆👆
It zoom-in to the touch points on double tap. Restore to default state when user double tap on max scale state.

### Pinch Zoom 👌
Zoom in and out with yout two finger.

### Free Mode 🤟
You can crop you image in free mode. In any size.

### Aspect Ratio Mode 📱
Enhanced aspect ratio mode will helps you while cropping. Aspect ratio will be fixed while you playing with cropper. So in your social media apps, it will helps you to crop in fixed size (instagram, facebook, twitter, 16:9, 1:2, 3:2 and more..)

### Size Displayer 🔟
While you scaling your image, size displayer displays the bitmap size reactively. It will give the user smoother experience.

### Auto Centered 😍
What ever you do while cropping, we centerized the bitmap with animation. Smoother experience for user.

### Animations 🌟 
We user animation everywhere in this cropper. User zoom-out too much? We zoom-in back with animation. User scroll image out of borders? We scroll it back with animation.

# Setup
```groovy
allprojects {
     repositories {
	...
	maven { url 'https://jitpack.io' }
     }
}
```
```groovy
dependencies {
      implementation 'com.github.lyrebirdstudio:Croppy:0.1'
}
```

License
--------


    Copyright 2017 Lyrebird Studio.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


