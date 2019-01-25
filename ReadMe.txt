Detect water from tif images
 -UNN (unet neural net) for detecting water bodies
     -Adam optimiser
     -Relu activation
     -512x512 input with 3 bands
     -resulting a model that creates masks with water bodies
     Network was trained using tensorflow-gpu, with requirements:
     -GPU min Nvidia 1050 2GB (the batch size should be reduced it, for a weaker gpu)
     -python 3.6.x
     -anaconda
     -Cuda
     -Nvidia Gpu Computing Toolkit
     -CuDNN
     -tensorlow
     -keras
     -tensorflow-gpu
     
    Java image processing:
        -detect water bodies from .tif images
        -class for creating masks for unet resulted from combining green wave_bands and NIF band .tif images, calculate NDIW(normalized  water index) NDWI per pixel = green - nif / green + nif , with  result >= 0.45 as water pixe;
        -class for creating subimages of 512x512 for UNet from Nif and masks, with 3 band RGB (without alpha!!)
        -class Make_difference takes an image which is not flooded and an image that is flooded and it makes an image that resambles the differences between the two
        -class CreateFloodMapBasedOnTOPO takes a nif image, the image resulted after the water detection algo is applied and the topographic image of that area (not necesarry layed perfectly over the nif image) because we use a method to identify areas based on the longitude, latitude coordinates
        
        Java requirement:
            -JDK 1.8.0 min
            -update lib ImageIO to jai-imageio-core-1.4.0 min (included in proj)
            -Gdal from python or http://download.gisinternals.com/sdk/downloads/release-1500-x64-gdal-2-2-3-mapserver-7-0-7/gdal-202-1500-x64-core.msi
            -need to add gdal libraty to java and copy:
                -gdalconstjni.dll
                -gdaljni.dll
                -ogrjni.dll
                -osrjni.dll   into java\jdk1.8.x...\bin from programfiles\gdal
            -from Environment Variables set JVM memory to at least 4GB with:
                _JAVA_OPTION - variable name
                -Xmx4096m - variable value
