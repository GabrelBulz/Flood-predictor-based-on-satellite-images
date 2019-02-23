package java_classes;


import javafx.util.Pair;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateFloodMapBasedOnTOPO {

    String source_files_path;
    String processed_files_path;
    int nr_sets;

    static class Thread_process_flood implements Runnable{

        String path_original_nif;
        String path_calculated_nif;
        String path_topo;
        String path_out_file;
        int set_nr;

        BufferedImage nif_img;
        BufferedImage result_nif_img;
        BufferedImage topo_img;


        public Thread_process_flood(String path_original_nif, String path_calculated_nif, String path_topo,
                                    int set_nr, String path_out_file)
        {
            this.path_original_nif = path_original_nif;
            this.path_calculated_nif = path_calculated_nif;
            this.path_topo = path_topo;
            this.path_out_file = path_out_file;
            this.set_nr = set_nr;

            File nif_file = new File(this.path_original_nif);
            File result_nif_file = new File(this.path_calculated_nif);
            File topo_file = new File(this.path_topo);

            this.nif_img = null;
            this.result_nif_img = null;
            this.topo_img = null;

            try {
                this.nif_img = ImageIO.read(nif_file);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("cannot load nif_file " + path_original_nif);
            }

            try {
                this.result_nif_img = ImageIO.read(result_nif_file);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("cannot load result_nif_file " + path_calculated_nif);
            }

            try {
                this.topo_img = ImageIO.read(topo_file);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("cannot load topo_file " + path_topo);
            }
        }

        @Override
        public void run() {

            Dataset dataset_topo;
            Dataset dataset_nif;

            double[] topo_transformation;
            double[] nif_transformation;

            SpatialReference topo_spatial_ref_1;
            SpatialReference topo_spatial_ref_2;
            SpatialReference nif_spatial_ref_1;
            SpatialReference nif_spatial_ref_2;

            gdal.AllRegister();

            dataset_nif = gdal.Open(this.path_original_nif, gdalconst.GA_ReadOnly);
            dataset_topo = gdal.Open(this.path_topo, gdalconst.GA_ReadOnly);

            /* height difference accepted */
            int height_diff = 1;

            if(this.nif_img != null && this.result_nif_img != null && this.topo_img != null){

                Stack<Pair< Pair<Integer, Integer>, Pair<Integer, Integer> >> stack;
                /**
                 * stack pair description
                 * first pair pixel coordinates for each cell wich is added
                 * second pair pixels of the cell that added current stack item --
                 * -- second pair <null, null> if the item is added by a water-pixel
                 *(use to determine the distance between 2 points)
                 */

                dataset_nif = gdal.Open(this.path_original_nif, gdalconst.GA_ReadOnly);
                dataset_topo = gdal.Open(this.path_topo, gdalconst.GA_ReadOnly);

                nif_transformation = new double[2];
                topo_transformation = new double[2];
                nif_transformation = dataset_nif.GetGeoTransform();
                topo_transformation = dataset_topo.GetGeoTransform();

                nif_spatial_ref_1 = new SpatialReference();
                nif_spatial_ref_1.ImportFromWkt(dataset_nif.GetProjectionRef());
                nif_spatial_ref_2 = new SpatialReference();
                nif_spatial_ref_2.ImportFromEPSG(4326);

                topo_spatial_ref_1 = new SpatialReference();
                topo_spatial_ref_1.ImportFromWkt(dataset_topo.GetProjectionRef());
                topo_spatial_ref_2 = new SpatialReference();
                topo_spatial_ref_2.ImportFromEPSG(4326);


                for (int i = 0; i < this.result_nif_img.getWidth(); i++) {
                    for (int j = 0; j < this.result_nif_img.getHeight(); j++) {
                        if (new Color(this.result_nif_img.getRGB(i, j)).getRed() == 255) {

                            int start_height = getHeightFromTopo(i, j, nif_transformation, nif_spatial_ref_1, nif_spatial_ref_2,
                                    topo_transformation, topo_spatial_ref_1, topo_spatial_ref_2, this.topo_img);

                            stack = new Stack<>();
                            Pair<Integer, Integer> start = new Pair<>(i, j);
                            Pair<Integer, Integer> added_by = new Pair<>(null, null);
                            stack.add(new Pair<>(start, added_by));

                            while (!stack.isEmpty()) {
                                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> stack_el = stack.pop();
                                Pair<Integer, Integer> xy = stack_el.getKey();
                                int x = xy.getKey();
                                int y = xy.getValue();

                                int current_color = new Color(this.result_nif_img.getRGB(x, y)).getRed();
                                Boolean mark = false;
                                Boolean mark_not_water = false;

                                //test if already water
                                if (current_color == 255)
                                    mark = true;
                                else {
                                    //test if height diff is in accepted limits
                                    if (new Color(result_nif_img.getRGB(x, y)).getGreen() != 255) {

                                        int current_height = getHeightFromTopo(x, y,nif_transformation, nif_spatial_ref_1, nif_spatial_ref_2,
                                                topo_transformation, topo_spatial_ref_1, topo_spatial_ref_2, this.topo_img);

                                        Pair<Integer, Integer> added_by_pix = stack_el.getValue();
                                        mark_not_water = true;
                                        if (added_by_pix.getKey() == null) {
                                            if (current_height >= start_height - (height_diff + 1) && current_height <= start_height + height_diff)
                                                mark = true;
                                        } else {
                                            double distance = calculateDistanceBetweenPoints(x, y, added_by_pix.getKey(), added_by_pix.getValue());
                                            if (current_height >= start_height - ((height_diff + 1) / (distance)) && current_height <= start_height + (height_diff / (distance))) {
                                                mark = true;
                                            }

                                        }
                                    }
                                }

                                if (mark) {
                                    //set color of current pixel to blue
                                    this.result_nif_img.setRGB(x, y, new Color(0, 255, 0).getRGB());
                                    // add neightbours
                                    if (x > 0)
                                        if (mark_not_water)
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x - 1, y), new Pair<Integer, Integer>(x, y)));
                                        else
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x - 1, y), new Pair<Integer, Integer>(null, null)));
                                    if (x < this.result_nif_img.getWidth() - 1)
                                        if (mark_not_water)
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x + 1, y), new Pair<Integer, Integer>(x, y)));
                                        else
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x + 1, y), new Pair<Integer, Integer>(null, null)));
                                    if (y > 0)
                                        if (mark_not_water)
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x, y - 1), new Pair<Integer, Integer>(x, y)));
                                        else
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x, y - 1), new Pair<Integer, Integer>(null, null)));
                                    if (y < this.result_nif_img.getHeight() - 1)
                                        if (mark_not_water)
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x, y + 1), new Pair<Integer, Integer>(x, y)));
                                        else
                                            stack.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x, y + 1), new Pair<Integer, Integer>(null, null)));
                                }
                            }
                        }
                    }
                }

                try {
                    ImageIO.write(this.result_nif_img, "PNG", new File(this.path_out_file + "\\" + Integer.toString(this.set_nr) + "_result.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public static double[] getCoordinatesFromPixel(double x, double y, double[] transformation, SpatialReference spat_ref1, SpatialReference spat_ref2){
        /**
         * @param x,y coordinates from pixel
         * @param transformation dataset. GetGeoTransform contains xoff, a, b, yoff, d, e
         * @return a array of double, first value is longitude and second latitude
         */
        double[] results = new double[2];

        results[0] = transformation[1]*x + transformation[2]*y + transformation[0];
        results[1] = transformation[4]*x + transformation[5]*y + transformation[3];

        CoordinateTransformation t = new CoordinateTransformation(spat_ref1, spat_ref2);
        results = t.TransformPoint(results[0], results[1]);

        return results;
    }


    public static double[] getPixelfromCoordinates(double logitude, double latitude, double[] transformation, SpatialReference spat1, SpatialReference spat2){
        /**
         * @return double array containing pixels were the coordinates may be found, first value is x second y
         */
        double[] result = new double[2];

        CoordinateTransformation coordTrans = new CoordinateTransformation(spat1, spat2);
        result = coordTrans.TransformPoint(logitude, latitude);

        result[0] = Math.round((result[0] - transformation[0]) / transformation[1]);
        result[1] = Math.round((result[1] - transformation[3]) / transformation[5]);

        return result;
    }

    public static double calculateDistanceBetweenPoints(int x1, int y1, int x2, int y2){
        /**
         * Basic math calculation of the distance between two points
         */
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static int getHeightFromTopo(int nif_x, int nif_y, double[] nif_transformation, SpatialReference nif_spatial_ref_1,
                                        SpatialReference nif_spatial_ref_2, double[] topo_transformation, SpatialReference topo_spatial_ref_1,
                                        SpatialReference topo_spatial_ref_2, BufferedImage topo_img){
        /**
         * @param x,y pixels into nif image
         * @return reutrn the height value into the topi image after calculation
         *         the coordinates are obtained from the nif image, then they are applied into the topi image resulting
         *         the pixels for that coordinates - the value of that pixel is returned
         */
        double[] coordinates_from_nif = getCoordinatesFromPixel(nif_x, nif_y, nif_transformation, nif_spatial_ref_1, nif_spatial_ref_2);
        double[] pixels_in_topo_from_coordinates = getPixelfromCoordinates(coordinates_from_nif[0], coordinates_from_nif[1], topo_transformation, topo_spatial_ref_1, topo_spatial_ref_2);

        int height = new Color(topo_img.getRGB((int)(pixels_in_topo_from_coordinates[0]), (int)(pixels_in_topo_from_coordinates[1]))).getRed();

        return height;
    }




    public CreateFloodMapBasedOnTOPO(String source_files_paht, String processed_files_path, int nr_sets){


        this.source_files_path = source_files_paht;
        this.processed_files_path = processed_files_path;
        this.nr_sets = nr_sets;


        ExecutorService excutor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < nr_sets; i++) {
            String original_nif = this.source_files_path + "\\" + Integer.toString(i) + "_nif.tif";
            String topo_path =   this.source_files_path + "\\" + Integer.toString(i) + "_topo.tif";
            String processed_nif = this.processed_files_path + "\\" + Integer.toString(i) + ".png";


            excutor.execute(new Thread_process_flood(original_nif, processed_nif, topo_path, i, this.processed_files_path));
        }



        excutor.shutdown();
        while(!excutor.isTerminated()){

        }


        System.out.println("Finish CREATEFLOOD");

    }


}
