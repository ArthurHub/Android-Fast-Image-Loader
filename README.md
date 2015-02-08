# Android Fast Image Loader   
New approach to image loading that overcomes Android memory management limitation by leveraging size, density, config, aggressive bitmap reuse and super-fast disk loading, to provide the fastest and most lightweight image handling possible.   

This library requires fine-tuning work, if you are just looking for simple library I would recommend: [Picasso][1], [Glide][2], [Universal Image Loader][3] or [Volley][4].   

### Quick Start   
```      
compile  'com.theartofdev.fastimageloader:fastimageloader:0.9.+'  (soon)
```   

soon…   

### Features   
 
* Smart memory and disk caching.  
* Super-fast, asynchronous, disk cache loading.  
* Asynchronous and parallel download.  
* Low memory footprint and optimizations for memory pressure.  
* Image services support ( [Thumbor ][5], [imgIX ][6], etc.)  
* Highly customizable specification loading (size/format/density/bitmap config/etc.)  
* Alternative specification loading.  
* Pre-fetch download images.  
* Advanced bitmaps reuse using inBitmap, reusing bitmaps on destroy and invisibility.  
* Smart prioritization and canceling of image load requests, more than list item reuse.  
* Placeholder, round rendering, fade-in animation support.  
* Extensive extensibility.  
* Logging and analytics hooks.  
* Debug indicator (loaded from Memory/Disk/Network).    
   

### Leverage Size, Density and Config   
There is a little benefit to download 1500x1500 pixel (450 ppi) image that will take **8.5 MB**  of memory to show 500x500 preview in a feed where the user scrolls quickly. It will be much better to decrees the image density by half and use 565 bitmap config lowering the memory footprint to **1 MB** , empirically, without loss of quality.   

### Aggressive bitmap reuse   
To limit the amount of memory allocated and GC work the library reused bitmaps as soon as they are no longer visible in the UI.   
 
* Activity/Fragment images that have not been destroyed but are no longer visible (onStop has been called) are eligible for bitmap reuse.  
* When the Activity/Fragment becomes visible again, if the bitmap was reused, the image is reload.  
* After initial allocations, bitmap reuse prevents almost all subsequent allocations.  
* High disk loading performance make this process seamless.    
   

### High disk cache performance   
 
* Up to 5-8 times faster load of cached images from disk.  
* Will add benchmark comparison…    
   

### License   
The MIT License (MIT)   
Copyright (c) 2015 Arthur Teplitzki   
See [license.md][7]     

[1]: http://square.github.io/picasso/
[2]: https://github.com/bumptech/glide
[3]: https://github.com/nostra13/Android-Universal-Image-Loader
[4]: https://github.com/mcxiaoke/android-volley
[5]: https://github.com/thumbor/thumbor
[6]: http://www.imgix.com/
[7]: https://github.com/ArthurHub/Android-Fast-Image-Loader/blob/master/license.md
