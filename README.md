PhotoSelect
====

What is The PhotoSelect?
----

##### PhotoSelect is a choice photo and take photo library that loading your ablum's photo with quickly. 

![Example Img](https://github.com/linsentmac/PhotoSelect/raw/master/photo/example.png)

How to compile the library?
----

Add it to your build.gradle with:

*project build.gradle*

```
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}
```


and:

*module build.gradle*

```
dependencies {
    compile 'com.github.linsentmac:PhotoSelect:{latest version}'
}
```

How to use PhotoSelect?
----

> start Activity


```
PhotoPicker.builder()
    	.setPhotoCount(20)
	.setShowCamera(true)
	.setSelected(selectedPhotos)
	.start(UploadImgActivity.this);
```

> Select Photo Callback


```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (resultCode == RESULT_OK
		&& (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

		List<String> photos = null;
		String cam_path = null;
		if (data != null) {
			photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
			cam_path = data.getStringExtra("callback_path");
		}

		if (photos != null) {
		    selectedPhotos.clear();
		    selectedPhotos.addAll(photos);
		    if(photos.size() > 0){
			 btn_add_no_img.setVisibility(View.GONE);
			 btn_add_img.setVisibility(View.VISIBLE);
		    }
		}

		if(cam_path != null){
		    selectedPhotos.add(cam_path);
		}
		    adapter.notifyDataSetChanged();
	}
}
```
