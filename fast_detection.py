from PIL import Image
import numpy as np


#PIl has a max_iamge pixels size of 80kk, and tif image are usaly bigger
Image.MAX_IMAGE_PIXELS = 130000000

image_name = 'test_river.tif'

im = Image.open(image_name)

image_np_arr = np.array(im)

for i in range(image_np_arr.shape[0]):
    for j in range(image_np_arr.shape[1]):
        if image_np_arr[i,j,0] <= 0  and image_np_arr[i,j,1] <= 0 and image_np_arr[i,j,2] <= 0:
            image_np_arr[i,j] = [0, 0, 0]
        else:
            image_np_arr[i,j] = [255, 255, 255]


im = Image.new('RGB', (image_np_arr.shape[0], image_np_arr.shape[1]))


for i in range(image_np_arr.shape[0]):
    for j in range(image_np_arr.shape[1]):
        im.putpixel((i,j), (image_np_arr[i,j,0], image_np_arr[i,j,1], image_np_arr[i,j,2]))

im = (im.transpose(Image.ROTATE_90)).transpose(Image.FLIP_TOP_BOTTOM)
im.save("result_test_algo.jpg")

