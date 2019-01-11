import numpy as np
import skimage.io as io
import skimage.transform as trans
from keras.preprocessing.image import ImageDataGenerator
import os

def saveResult(save_path,npyfile):
    for i,item in enumerate(npyfile):
        img = item[:,:,0]
        img[img >=0.5] = 1
        img[img <0.5] = 0
        io.imsave(os.path.join(save_path,"%d_predict.png"%i),img)


def create_image_gen(aug_dict, train_path, image_folder, image_color_mode, target_size,
                        batch_size, save_to_dir, image_save_prefix, seed):
    image_datagen = ImageDataGenerator(**aug_dict)
    image_generator = image_datagen.flow_from_directory(
        train_path,
        classes = [image_folder],
        class_mode = None,
        color_mode = image_color_mode,
        target_size = target_size,
        batch_size = batch_size,
        save_to_dir = save_to_dir,
        save_prefix  = image_save_prefix,
        seed = seed)

    return image_generator


def create_mask_gen(aug_dict, train_path, mask_folder, mask_color_mode, target_size,
                        batch_size, save_to_dir, mask_save_prefix, seed):
    mask_datagen = ImageDataGenerator(**aug_dict)
    mask_generator = mask_datagen.flow_from_directory(
        train_path,
        classes = [mask_folder],
        class_mode = None,
        color_mode = mask_color_mode,
        target_size = target_size,
        batch_size = batch_size,
        save_to_dir = save_to_dir,
        save_prefix  = mask_save_prefix,
        seed = seed)

    return mask_generator


def trainGenerator(batch_size,train_path,image_folder,mask_folder,aug_dict,image_color_mode = "grayscale",
                    mask_color_mode = "grayscale",image_save_prefix  = "image",mask_save_prefix  = "mask",
                    flag_multi_class = False,num_class = 2,save_to_dir = None,target_size = (512,512),seed = 1):

    image_generator = create_image_gen(aug_dict, train_path,image_folder, image_color_mode, target_size,
                                        batch_size, save_to_dir, image_save_prefix, seed)
    mask_generator = create_mask_gen(aug_dict, train_path, mask_folder, mask_color_mode, target_size,
                                        batch_size, save_to_dir, mask_save_prefix, seed)

    train_generator = zip(image_generator, mask_generator)
    for (img,mask) in train_generator:
        img,mask = adjustData(img,mask,flag_multi_class,num_class)
        yield (img,mask)


def adjustData(img,mask,flag_multi_class,num_class):
    img = img / 255
    mask = mask /255
    mask[mask > 0.5] = 1
    mask[mask <= 0.5] = 0
    return (img,mask)


def testGenerator(test_path,num_image = 0,target_size = (512,512),flag_multi_class = False,as_gray = True):
    for i in range(num_image):
        img = io.imread(os.path.join(test_path,"%d.png"%i),as_gray = as_gray)
        img = img / 255
        img = trans.resize(img,target_size)
        #print(np.shape(img))
        img = np.reshape(img,img.shape+(1,))
        img = np.reshape(img,(1,)+img.shape)
        #print(np.shape(img))
        yield img