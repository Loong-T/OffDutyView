# OffDutyView
An android library to implement dragging to disappear(explode) feature

## Sample

Download [sample app](https://github.com/Loong-T/OffDutyView/releases/download/1.0.0/sample.apk).

## Screenshots

![Simple](https://raw.githubusercontent.com/Loong-T/OffDutyView/master/screenshots/offduty_simple.gif)

![List](https://raw.githubusercontent.com/Loong-T/OffDutyView/master/screenshots/offduty_list.gif)

![Custom](https://raw.githubusercontent.com/Loong-T/OffDutyView/master/screenshots/offduty_custom.gif)

## Usage

Add the JitPack repository to your root build file:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add OffDutyView to your dependencies:
```groovy
dependencies {
    implementation 'in.nerd-is:OffDutyView:1.0.0'
}
```

Use OffDutyView as a normal view:
```xml
<in.nerd_is.offdutyview.OffDutyView
    android:id="@+id/offDutyView"
    android:layout_width="16dp"
    android:layout_height="16dp"
    android:layout_marginTop="32dp"
    android:background="@drawable/red_dot_bg"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/btnReset"
    app:oddBezierColor="#41e9ff"
    app:oddMaxDistance="128dp" />
```

```kotlin
offDutyView.setOnOffDutyListener {
    Toast.makeText(this, "Off duty!", Toast.LENGTH_SHORT).show()
}
offDutyView.setOnCancelListener {
    Toast.makeText(this, "Off duty canceled", Toast.LENGTH_SHORT).show()
}
btnReset.setOnClickListener {
    offDutyView.resetView()
}
```

If you want to show some text, e.g. a number, use OffDutyTextView instead.

### Custom view

You can also add OffDuty feature to other views easily, refer to [OffDutyImageView](https://github.com/Loong-T/OffDutyView/blob/master/app/src/main/java/in/nerd_is/offdutyview/sample/CustomOffDutyImageView.java).

## Reference

[让创意落地！QQ手机版5.0“一键下班”设计小结](https://www.uisdc.com/qq-1-key-off-work)

## License
```
Copyright 2019 Xuqiang ZHENG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
