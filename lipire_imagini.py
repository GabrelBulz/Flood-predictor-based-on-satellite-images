from PIL import Image
import numpy as np


image_name1 = "mai_rapid2.jpg"
image_name2 = 't08_4_final_edge3_custom_threshold50.tif'

im1 = Image.open(image_name1)

image_np_arr1 = np.array(im1)

im2 = Image.open(image_name2)

image_np_arr2 = np.array(im2)

size = image_np_arr1.shape

img3 = np.zeros(size, dtype=int)


# print(image_np_arr1.shape)
# print(image_np_arr2.shape)

for i in range(size[0]):
    for j in range(size[1]):
        if image_np_arr1[i,j,0] == 255 or image_np_arr2[j,i] == 255:
            img3[i,j] = [255, 255, 255]


im = Image.new('RGB', (size[0], size[1]))


for i in range(size[0]):
    for j in range(size[1]):
        im.putpixel((i,j), (img3[i,j,0], img3[i,j,1], img3[i,j,2]))

im.save("mai_rapid2.jpg")


