import cv2

from PIL import Image

image_name = 't08_4_final.tif'

img = cv2.imread(image_name)

# blur = cv2.GaussianBlur(img,(5,5),0)
# sobelx = cv2.Sobel(img,cv2.CV_64F,1,0,ksize=5)
# sobely = cv2.Sobel(sobelx,cv2.CV_64F,0,1,ksize=5)


# cv2.imwrite('sobely.jpg', sobely)
# cv2.imwrite('sobelx.jpg', sobelx)

# img = cv2.imread('sobely.jpg')
edges = cv2.Canny(img,200,200)
cv2.imwrite('edge.jpg', edges)