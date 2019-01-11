from PIL import Image
import numpy as np
from numba import cuda
import json

#@cuda.jit
def tif_np_arr_compressed_for_grey(image_np_arr):
    """
        Because a gray image has the rgb values equal,
        we will need to get rid of the extra values,
        only one value is enough (ex save only red values)
    """
    shape_original_img = image_np_arr.shape

    essential_arr = np.zeros([shape_original_img[0], shape_original_img[1]], dtype=int)

    for i in range(shape_original_img[0]):
        for j in range(shape_original_img[1]):
            essential_arr[i,j] = image_np_arr[i,j,0]

    return essential_arr



def int_to_8_bit_representation(int_to_represent):

    result = list(map(int,format(int_to_represent, 'b')))

    # representation may be shorter then 1 byte, ex 5 101
    # add nr of zeros needed to be 1 byte representation
    aux_to_add = list(np.zeros([8 - len(result)], dtype=int))

    return aux_to_add + result


def np_arr_img_To_bitplanes(image_np_arr):
    """
        Get an 2D np_array such as an essential arr
        and transforms it into 8 bits bitplanes
        return resulting arr of dimension N x N x 8
    """

    shape_original_img = image_np_arr.shape

    np_arr_bitplane = np.zeros([shape_original_img[0], shape_original_img[1], 8], dtype=int)

    for i in range(shape_original_img[0]):
        for j in range(shape_original_img[1]):
            np_arr_bitplane[i,j] = int_to_8_bit_representation(image_np_arr[i,j])

    return np_arr_bitplane


def split_np_array_bitplanes_into_separate_bitmaps(np_arr_bitplanes):
    bitplanes = {}

    shape_np_arr_bitplanes = np_arr_bitplanes.shape

    for plane in range(8):

        bit_plane = np.zeros([shape_np_arr_bitplanes[0], shape_np_arr_bitplanes[1]], dtype=int)

        for i in range(shape_np_arr_bitplanes[0]):
            for j in range(shape_np_arr_bitplanes[1]):
                bit_plane[i,j] = np_arr_bitplanes[i, j, plane]

        bitplanes[plane] = bit_plane

    return bitplanes


def create_img_bitmaps(bitplne, nr):
    """
        Creates 8 images, correponding to each bit plane
        Only for presentation purpose
        Can be ignored
    """

    shape_img = bitplne.shape

    size = shape_img[0], shape_img[1]

    im = Image.new('RGB', size)


    for i in range(shape_img[0]):
        for j in range(shape_img[1]):
            im.putpixel((i,j), (bitplne[i,j]*255, bitplne[i,j]*255, bitplne[i,j]*255))


    im = (im.transpose(Image.ROTATE_90)).transpose(Image.FLIP_TOP_BOTTOM)

    name_img = 'bit_plane' + str(nr) + '.jpg'
    im.save(name_img)


def run_lenght(arr):
    """
        run lenght applyed on an array
        works on consecutive strings of 1's
        return a list with lists
    """
    x = []
    cont = 0
    mark_index = None

    i=0
    while i < len(arr):
        if arr[i] == 1:
            mark_index = i
            j = i+1
            while j < len(arr) and arr[j] == 1:
                j+=1


            x.append([mark_index, j-i])
            i=j
        i+=1

    return x


# def run_length_encoded_to_tif(encoded_list):
#     """
#         Gets an run length encoded list and create a .tif format
#         This is done to be able to compare the actual sizes
#     """
#     size_x = len(encoded_list)
#     size_y = 0

#     for i in encoded_list:
#         if size_y < len(i):
#             size_y = len(i)

#     tiff_np_arr = np.zeros([size_x, size_y,3], dtype=int)

#     try:
#         for i in encoded_list:
#             if len(i) > 0:
#                 for j in i:
#                     if len(j) > 0:
#                         tiff_np_arr[encoded_list.index(i), i.index(j)] = [j[0], j[1], 0]
#     except:
#         print("Bad format encoded_list")

#     print(tiff_np_arr.shape)

        #cannot use this function because there may be an occurence of 1's
        #at line 300 or so... and the images does not suppot values bigger
        #than 255 (pixel value)
#     encoded_tif = Image.fromarray(tiff_np_arr)
#     encoded_tif.save('encoded_image.tif')


def create_diff_image_orinial_and_bitplane(name_original, name_bitplane):
    original = Image.open(name_original)
    bitplane = Image.open(name_bitplane)

    np_original = np.array(original)
    np_bitplane = np.array(bitplane)

    size = (np_original.shape[0], np_original.shape[1])

    result_image = Image.new('RGB', size)

    cont = 0

    for i in range(size[0]):
        for j in range(size[1]):
            if np_original[i,j,0] == 0 and np_bitplane[i,j,0] == 0:
                result_image.putpixel((i,j), (np_bitplane[i,j,0], np_bitplane[i,j,1], np_bitplane[i,j,2]))
                cont += 1
            else:
                result_image.putpixel((i,j), (255, 255, 255))


    print('Nr of black pixels found in diff image ' + str(cont))
    print('Which means ' + str((cont * 0.001)) + ' km^2 of surface water from a total of ' + str(size[0]*size[1]*0.001) +' km^2')

    result_image = (result_image.transpose(Image.ROTATE_90)).transpose(Image.FLIP_TOP_BOTTOM)
    result_image.save('result_diff_image.jpg')


def main():

    #PIl has a max_iamge pixels size of 80kk, and tif image are usaly bigger
    Image.MAX_IMAGE_PIXELS = 130000000

    image_name = 't08_4_final.tif'
    bitplane_diff_image_name = 'bit_plane3.jpg'

    im = Image.open(image_name)

    image_np_arr = np.array(im)

    with open('img_tif.json', 'w') as tifj:
        json.dump(image_np_arr.tolist(), tifj)

    ###for cuda
    # shape_original_img = image_np_arr.shape
    # essential_arr = np.zeros([shape_original_img[0], shape_original_img[1]], dtype=int)

    # # Set the number of threads in a block
    # threadsperblock = 50

    # # Calculate the number of thread blocks in the grid
    # blockspergrid = (image_np_arr.size + (threadsperblock - 1)) // threadsperblock


    # tif_np_arr_compressed_for_grey[blockspergrid, threadsperblock](image_np_arr, essential_arr)

    # print(tif_np_arr_compressed_for_grey(image_np_arr))

    ######

    res = np_arr_img_To_bitplanes(tif_np_arr_compressed_for_grey(image_np_arr))
    bitplanes = split_np_array_bitplanes_into_separate_bitmaps(res)


    bitplanes_encoded = {}

    for i in range(8):

        final_list = []

        for j in range(bitplanes[i].shape[0]):
            final_list.append(run_lenght(bitplanes[i][j]))

        bitplanes_encoded[i] = final_list


    with open('bit1.json', 'w') as outfile:
        for i in range(8):
            json.dump(bitplanes[i].tolist(), outfile)

    for i in bitplanes:
        create_img_bitmaps(bitplanes[i], i)

    create_diff_image_orinial_and_bitplane(image_name, bitplane_diff_image_name)

main()