package java_classes;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.*;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class Process_image {


    public static void main(String []args) throws IOException {
        /**
         * This will run a water detector algorithm over a set of files
         * After the water is detected a flood algorithm based on topo will be applied
         *
         * @param - the params are passed through command line args
         *          first param is the path to the file where the images are unziped
         *          second param is the path to the file where the processed images will be stored
         */
         String source_images = args[0];
         String out_processed_images = args[1];
         int nr_images = new File(source_images).listFiles().length / 3;


        ///just exclude the 3'rd image bad format
        WaterDerectorBasic w = new WaterDerectorBasic(source_images, nr_images, out_processed_images);
        CreateFloodMapBasedOnTOPO flood_predictor = new CreateFloodMapBasedOnTOPO(source_images, out_processed_images, nr_images);
//         Subimages_512 sub = new Subimages_512(21, path_source_images, out_splited_source_images, path_processed_images, out_splited_processed_images);


//        CreateFloodMapBasedOnTOPO flood = new CreateFloodMapBasedOnTOPO(result_nif_path, nif_path, topo_path);
//        make_difference diff= new make_difference("D:\\proj_licenta\\set_images_landsat\\processed_landsat\\7.png", "D:\\proj_licenta\\set_images_landsat\\processed_landsat\\out7.png");




    }
}
