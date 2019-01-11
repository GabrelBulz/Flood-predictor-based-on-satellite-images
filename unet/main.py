from data import trainGenerator, testGenerator, saveResult
from model import unet
from keras.callbacks import ModelCheckpoint
from keras.preprocessing.image import ImageDataGenerator

path_input = "images/data"
path_mask = "images/data"
path_test_result = "images/test"


data_gen_args = dict(rotation_range=0.2,
                    width_shift_range=0.05,
                    height_shift_range=0.05,
                    shear_range=0.05,
                    zoom_range=0.05,
                    horizontal_flip=True,
                    fill_mode='nearest')

myGene = trainGenerator(2,path_input,'images_splited','processed_images_splited',data_gen_args, save_to_dir = 'images/aug')

model = unet('water_derection_unet_weights.hdf5')
model_checkpoint = ModelCheckpoint('water_derection_unet_weights.hdf5', monitor='loss',verbose=1, save_best_only=True)
model.fit_generator(myGene,steps_per_epoch=300,epochs=3,callbacks=[model_checkpoint])


# testGene = testGenerator(path_test_result,5)
# results = model.predict_generator(testGene,5,verbose=1)
# saveResult(path_test_result,results)
