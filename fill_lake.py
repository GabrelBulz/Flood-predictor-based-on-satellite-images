from PIL import Image
import numpy as np





# def fill(data, start_coords, fill_value):
#         """
#     Flood fill algorithm

#     Parameters
#     ----------
#     data : (M, N) ndarray of uint8 type
#         Image with flood to be filled. Modified inplace.
#     start_coords : tuple
#         Length-2 tuple of ints defining (row, col) start coordinates.
#     fill_value : int
#         Value the flooded area will take after the fill.

#     Returns
#     -------
#     None, ``data`` is modified inplace.
#     """
#     xsize, ysize = data.shape
#     orig_value = data[start_coords[0], start_coords[1]]

#     stack = set(((start_coords[0], start_coords[1]),))

#     orig_value == 0

#     while stack:
#         x, y = stack.pop()

#         if data[x, y] == orig_value:
#             data[x, y] = fill_value
#             if x > 0:
#                 stack.add((x - 1, y))
#             if x < (xsize - 1):
#                 stack.add((x + 1, y))
#             if y > 0:
#                 stack.add((x, y - 1))
#             if y < (ysize - 1):
#                 stack.add((x, y + 1))


def fill(data, start_coords, fill_value, data2):

    xsize, ysize= (data.shape[0], data.shape[1])
    orig_value = 0
    stack = set(((start_coords[0], start_coords[1]),))

    while stack:
        x, y = stack.pop()
        # print(str(x) + ' ' + str(y))

        # print(str(x) + ' ' + str(y))

        if data[x, y, 0] == orig_value:
            data[x, y, 0] = fill_value
            data2[x, y, 0] = fill_value
            if x > 0:
                stack.add((x-1, y))
            if x < (xsize - 1):
                stack.add((x + 1, y))
            if y > 0:
                stack.add((x, y - 1))
            if y < (ysize - 1):
                stack.add((x, y + 1))


img_edge_name = 't08_4_final_edge3_custom_threshold50.tif'
img_incomplete_name = "result_my_algo.jpg"

im_edge = Image.open(img_edge_name)
im_incomplete = Image.open(img_incomplete_name)

np_incomp = np.array(im_incomplete)
np_edge = np.array(im_edge)


size_inc = np_incomp.shape
size_egde = np_edge.shape


print(size_egde)
print(size_inc)

for i in range(size_inc[0]):
    for j in range(size_inc[1]):
        if np_incomp[i,j,0] == 255:
            fill(np_edge, (i,j), 255, np_incomp)
    print(i)



im = Image.new('RGB', (size_inc[0], size_inc[1]))


for i in range(size_inc[0]):
    for j in range(size_inc[1]):
        im.putpixel((i,j), (np_incomp[i,j,0], np_incomp[i,j,0], np_incomp[i,j,0]))

im = (im.transpose(Image.ROTATE_90)).transpose(Image.FLIP_TOP_BOTTOM)
im.save("result_fill_lake.jpg")

