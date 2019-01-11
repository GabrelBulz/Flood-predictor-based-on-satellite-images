import cv2
import numpy as np
from matplotlib import pyplot as plt
from PIL import Image

# img = cv2.imread('bit_plane5.jpg')

# kernel = np.ones((5,5),np.float32)/25
# dst = cv2.fastNlMeansDenoising(img,None,10,7,21)

# plt.subplot(121),plt.imshow(img),plt.title('Original')
# plt.xticks([]), plt.yticks([])
# plt.subplot(122),plt.imshow(dst),plt.title('Averaging')
# plt.xticks([]), plt.yticks([])
# plt.show()

image_name = 't08_4.tif'

img = cv2.imread(image_name)

size = img.shape

for i in range(size[0]):
    for j in range(size[1]):
        if img[i,j,0] >= 10 :
            img[i,j] = [255,255,255]

cv2.imwrite('cv2.jpg', img)
