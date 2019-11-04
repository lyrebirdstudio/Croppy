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

# Custom Usage

Create cropped image result in external storage
```kotlin
val externalCropRequest = CropRequest.Auto(sourceUri = uri, requestCode = RC_CROP_IMAGE)
```

Create cropped image result in cache storage
```kotlin
val cacheCropRequest = CropRequest.Auto(
     sourceUri = uri,
     requestCode = RC_CROP_IMAGE,
     storageType = StorageType.CACHE
)
```

If you want to create destination file manually

```kotlin
val destinationUri = ...
val manuelCropRequest = CropRequest.Manual(
    sourceUri = uri,
    destinationUri = destinationUri,
    requestCode = RC_CROP_IMAGE
)
```

If you want to exclude some specific aspect ratio from bottom aspect ratio list view.

```kotlin
val excludeAspectRatiosCropRequest = CropRequest.Manual(
    sourceUri = uri,     
    destinationUri = destinationUri,
    requestCode = RC_CROP_IMAGE,
    excludedAspectRatios = arrayListOf(AspectRatio.ASPECT_FREE)
)
```

If you want to give specific theme as accent color.
```kotlin
val themeCropRequest = CropRequest.Manual(
    sourceUri = uri,
    destinationUri = destinationUri,
    requestCode = RC_CROP_IMAGE,
    croppyTheme = CroppyTheme(R.color.blue)
)
```

```kotlin
//Start croppy with your custom request.
Croppy.start(this, cropRequest)
```

# Demo
<img src="https://github.com/lyrebirdstudio/Croppy/blob/master/art/artgif.gif?raw=true"/>

# Features

### Double tap focus 👆👆
It zooms-in to the touch points on double tap. Restores default state when user double taps on max scale state.

### Pinch Zoom 👌
Zoom in and out with two finger.

### Free Mode 🤟
You can crop your image in free mode. In any size.

### Aspect Ratio Mode 📱
Enhanced aspect ratio mode will help you while cropping. Aspect ratio will be fixed while you play with cropper. So for your social media apps, it will help you to crop in fixed size (instagram, facebook, twitter, 16:9, 1:2, 3:2 and more..)

### Size Displayer 🔟
While you scale your image, size displayer indicates the bitmap size reactively. It will provide a smoother experience to the user.

### Auto Centered 😍
What ever you do while cropping, we center the bitmap with animation. Smoother experience for user.

### Animations 🌟 
We use animation everywhere in this cropper. User zoomed-out too much? We zoom back in with animation. User scrolled image out of borders? We scroll it back with animation.

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


    Copyright 2019 Lyrebird Studio.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


