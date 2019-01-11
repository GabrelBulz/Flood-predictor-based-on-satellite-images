from PIL import Image
import numpy as np
from numba import cuda

@cuda.jit
def tif_np_arr_compressed_for_grey(image_np_arr, essential_arr):
    """
        Because a gray image has the rgb values equal,
        we will need to get rid of the extra values,
        only one value is enough (ex save only red values)
    """
    shape_original_img = image_np_arr.shape

    # essential_arr = np.zeros([shape_original_img[0], shape_original_img[1]], dtype=int)

    for i in range(shape_original_img[0]):
        for j in range(shape_original_img[1]):
            essential_arr[i,j] = image_np_arr[i,j,0]

    # return essential_arr


def main():

    #PIl has a max_iamge pixels size of 80kk, and tif image are usaly bigger
    Image.MAX_IMAGE_PIXELS = 130000000

    image_name = 't08_4_1.tif'

    im = Image.open(image_name)

    image_np_arr = np.array(im)


    ##for cuda
    shape_original_img = image_np_arr.shape
    essential_arr = np.zeros([shape_original_img[0], shape_original_img[1]], dtype=int)

    # Set the number of threads in a block
    threadsperblock = 50

    # Calculate the number of thread blocks in the grid
    blockspergrid = (image_np_arr.size + (threadsperblock - 1)) // threadsperblock


    tif_np_arr_compressed_for_grey[blockspergrid, threadsperblock](image_np_arr, essential_arr)

    # print(tif_np_arr_compressed_for_grey(image_np_arr))


main()