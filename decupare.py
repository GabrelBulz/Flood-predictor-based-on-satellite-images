from PIL import Image
import numpy as np


img_edge_name = 't08_4_final_edge2_threshold50.tif'
img_incomplete_name = "result_my_algo.jpg"

im_edge = Image.open(img_edge_name)
im_incomplete = Image.open(img_incomplete_name)

np_incomp = np.array(im_incomplete)
np_edge = np.array(im_edge)


size_inc = np_incomp.shape
size_egde = np_edge.shape

im = Image.new('RGB', (200, 200))


for i in range(200):
    for j in range(200):
        im.putpixel((i,j), (np_incomp[i,j,0], np_incomp[i,j,0], np_incomp[i,j,0]))

im.save("result_my_algo1.jpg")


for i in range(200):
    for j in range(200):
        im.putpixel((i,j), (np_edge[i,j], np_edge[i,j], np_edge[i,j]))

im.save('t08_4_final_edge2_threshold501.tif')