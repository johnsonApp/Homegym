package com.jht.homegym.algorithm;

import android.util.Log;

import com.jht.homegym.utils.Utils;

public class AccessoryExercise {

    private static final String  TAG = "AccessoryExercise";

    // Sensor raw data
    private static final float const_gravity = 9.81f;
    private float SENSOR_FILER_ALPHA = 0.2f, acceX = 0.0f, acceY = 0.0f, acceZ = 0.0f;
    private float[] acce_gravity = new float[3];
    private float[][] acce_point = new float[9][2];
    private float acce_gravity_get = 0.0f, acce_gravity_sub = 0.0f, acce_gravity_sub_temp = 0.0f, acce_gravity_filter_get = 0.0f;
    private static final short filter_length = 5, filter_length_1 = filter_length-1;
    private float[] acceGet_filter_buffer = new float[filter_length];
    private float[] acceX_filter_buffer = new float[filter_length];
    private float[] acceY_filter_buffer = new float[filter_length];
    private static final short acce_maxDataPoints = 60;
    private byte acceX_index = 0, index_check_pre = 0, index_check_sub = 0;
    private float[] acceGet_gravity_buffer = new float[acce_maxDataPoints];
    private float[] acceX_gravity_buffer = new float[acce_maxDataPoints];
    private float[] acceY_gravity_buffer = new float[acce_maxDataPoints];
    private float acce_avg_new = 0.0f, acce_avg_new_x = 0.0f, acce_avg_new_y = 0.0f, acce_std = 0.0f, acce_std_x = 0.0f, acce_std_y = 0.0f;
    private float acce_avg = 0.0f;
    private byte acce_choose = 0;

    // Dumbbell counting
    private boolean isUp = false;
    private int exercise_counter = 0;
    private short active_counter = 0;

    // Jump rope counting
    private float angle = 0.0f;
    private byte[] angle_record = new byte[acce_maxDataPoints+1];
    private byte[] angle_counter = new byte[3];
    private byte[] angle_counter_top10 = new byte[3];
    private int cw_counter = 0, ccw_counter = 0, temp_counter = 0;
    private byte checkAccessoryMode = 0, positive_counter = 0;
    private boolean isRotating = false;

    // Threshold
    private float acce_gravity_sub_threshold = 0.3f, acce_gravity_std_threshold = 1.0f;
    private byte active_threshold = 15, timestamp_threshold = 5;

    public AccessoryExercise(){
    }

    public AccessoryExercise(int accessoryMode){
        if(accessoryMode == Utils.DUMBBELL) {
            this.acce_gravity_sub_threshold = 0.3f;
            this.timestamp_threshold = 5;
            this.acce_gravity_std_threshold = 1.0f;
            this.checkAccessoryMode = Utils.DUMBBELL;
        }
        else if(accessoryMode == Utils.ROPE_SKIP) {
            this.acce_gravity_sub_threshold = 0.3f; // 1.0f
//            this.timestamp_threshold = 0;
            this.acce_gravity_std_threshold = 1.0f;
            this.checkAccessoryMode = Utils.ROPE_SKIP;
        }
    }

    public int exerciseCounting(final float x, final float y, final float z) {
        if(this.checkAccessoryMode == Utils.DUMBBELL){
            return dumbbellCounting(x, y, z);
        }
        else if(this.checkAccessoryMode == Utils.ROPE_SKIP){
            return jumpRopeCounting(x, y, z);
        }
        else return 0;
    }

    public int dumbbellCounting(final float x, final float y, final float z) {
        // Sensor row data
        acceX = x;
        acceY = y;
        acceZ = z;
        // Low-pass filter to reduce noise
        // to isolate the force of gravity
        acce_gravity[0] = SENSOR_FILER_ALPHA * acceX + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[0];
        acce_gravity[1] = SENSOR_FILER_ALPHA * acceY + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[1];
        acce_gravity[2] = SENSOR_FILER_ALPHA * acceZ + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[2];

        // Check to get gravity x, y or z
        if(active_counter < 10){
            if(Math.abs(acce_gravity[0]) > Math.abs(acce_gravity[2])){
                if(Math.abs(acce_gravity[0]) > Math.abs(acce_gravity[1])) acce_choose = 0;
                else acce_choose = 1;
            }
            else{
                if(Math.abs(acce_gravity[2]) > Math.abs(acce_gravity[1])) acce_choose = 2;
                else acce_choose = 1;
            }
        }
        switch (acce_choose){
            case 0:
                acce_gravity_get = acce_gravity[0];
                break;
            case 1:
                acce_gravity_get = acce_gravity[1];
                break;
            case 2:
                acce_gravity_get = acce_gravity[2];
                break;
        }

        // Buffer shift and apply average filter
        // Subtraction
        acce_gravity_filter_get = 0.0f;
        acce_gravity_sub = 0.0f;
        for(int i=0; i<filter_length_1; i++){
            acce_gravity_filter_get += acceGet_filter_buffer[i];
            acce_gravity_sub_temp = Math.abs((acceGet_filter_buffer[i+1] - acceGet_filter_buffer[i]));
            if(acce_gravity_sub_temp < const_gravity)
                acce_gravity_sub += acce_gravity_sub_temp;
            acceGet_filter_buffer[i] = acceGet_filter_buffer[i+1];
        }
        acce_gravity_filter_get += acceGet_filter_buffer[filter_length_1];
        acce_gravity_filter_get = acce_gravity_filter_get/(float) filter_length;
        acce_gravity_sub = acce_gravity_sub/(float) filter_length_1;
        acceGet_filter_buffer[filter_length_1] = acce_gravity_get;

        // Keep selected gravity in buffer
        if(acceX_index >= acce_maxDataPoints) acceX_index = 0;
        acceGet_gravity_buffer[acceX_index] = acce_gravity_get;
        acceX_index++;

        // Calculate average
        // Calculate standard deviation
        acce_avg_new = 0.0f;
        acce_std = 0.0f;
        for(int i=0; i<acce_maxDataPoints; i++){
            acce_avg_new += acceGet_gravity_buffer[i];
            acce_std += (float) Math.pow(acceGet_gravity_buffer[i], 2);
        }
        acce_avg_new = acce_avg_new / (float) acce_maxDataPoints;
        acce_std = acce_std / (float) acce_maxDataPoints;
        acce_std = (float) Math.sqrt( acce_std - Math.pow(acce_avg_new, 2));

        // Continue active count
        if(acce_gravity_sub > acce_gravity_sub_threshold){
            if(active_counter < active_threshold) active_counter++;
        }else{
            if(active_counter > 0) active_counter--;
        }

        // Continue active check
        // Update average
        if(active_counter > 10)
            acce_avg = acce_gravity_filter_get*SENSOR_FILER_ALPHA + (1.0f - SENSOR_FILER_ALPHA)*acce_avg;

        // Check exercise counting
        if (!isUp) {
            if (acce_gravity_get > acce_avg) {
                // Check X, Z exchange suddenly
                if(Math.abs(this.acce_gravity_get-this.acceGet_filter_buffer[filter_length_1-1]) < const_gravity){
                    // Check exercising
                    if(acce_gravity_sub > acce_gravity_sub_threshold && acce_std > acce_gravity_std_threshold){
                        // Check counting timestamp
                        if(acceX_index < index_check_pre) index_check_sub = (byte) (acceX_index + acce_maxDataPoints - index_check_pre);
                        else index_check_sub = (byte) (acceX_index - index_check_pre);

                        // If counting time is long enough then count
                        if(index_check_sub > timestamp_threshold){
                            exercise_counter++;
                        }
                        isUp = true;
                        index_check_pre = acceX_index;
                    }
                } else isUp = true;
            }
        } else {
            if (acce_gravity_get < acce_avg) {
                // Check exercising
                isUp = false;
            }
        }

        return exercise_counter;
    }

    public int jumpRopeCounting(final float x, final float y, final float z) {
        // Sensor row data
//        acceX = (x-(-10.32f))/20.37f*19.62f+(-9.81f);
//        acceY = (y-(-9.89f))/20.36f*19.62f+(-9.81f);
//        acceZ = (z-(-9.48f))/20.30f*19.62f+(-9.81f);
        acceX = x;
        acceY = y;
        acceZ = z;
        // Low-pass filter to reduce noise
        // to isolate the force of gravity
        acce_gravity[0] = SENSOR_FILER_ALPHA * acceX + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[0];
        acce_gravity[1] = SENSOR_FILER_ALPHA * acceY + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[1];
//        acce_gravity[2] = SENSOR_FILER_ALPHA * acceZ + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[2];

        acce_gravity_get = (Math.abs(acce_gravity[0]) + Math.abs(acce_gravity[1]));// / 2.0f;

        // Buffer shift and apply average filter
        // Subtraction
        // filter_length = 5
        acce_gravity_filter_get = 0.0f;
        acce_gravity_sub = 0.0f;
        acceX = 0.0f;
        acceY = 0.0f;
        for(int i=0; i<filter_length_1; i++){
            acce_gravity_filter_get += acceGet_filter_buffer[i];
            acceX += acceX_filter_buffer[i];
            acceY += acceY_filter_buffer[i];
            acce_gravity_sub_temp = Math.abs((acceGet_filter_buffer[i+1] - acceGet_filter_buffer[i]));
            if(acce_gravity_sub_temp < const_gravity)
                acce_gravity_sub += acce_gravity_sub_temp;
            acceGet_filter_buffer[i] = acceGet_filter_buffer[i+1];
            acceX_filter_buffer[i] = acceX_filter_buffer[i+1];
            acceY_filter_buffer[i] = acceY_filter_buffer[i+1];
        }
        acce_gravity_filter_get += acceGet_filter_buffer[filter_length_1];
        acce_gravity_filter_get /= (float) filter_length; // For acce_avg
        acceX += acceX_filter_buffer[filter_length_1];
        acceX /= (float) filter_length; // For acce_avg_x
        acceY += acceY_filter_buffer[filter_length_1];
        acceY /= (float) filter_length; // For acce_avg_y
        acce_gravity_sub /= (float) filter_length_1; // For check acce_gravity_sub > acce_gravity_sub_threshold
        acceGet_filter_buffer[filter_length_1] = acce_gravity_get;
        acceX_filter_buffer[filter_length_1] = acce_gravity[0];
        acceY_filter_buffer[filter_length_1] = acce_gravity[1];

        // Keep 5 points of X, Y
        for(int i=0; i<8; i++){
            acce_point[i][0] = acce_point[i+1][0];
            acce_point[i][1] = acce_point[i+1][1];
        }
        // Select X, Y for current accessory design
        acce_point[8][0] = acceX; //acce_gravity[0];
        acce_point[8][1] = acceY; //acce_gravity[1];

        // acce_point index : 0 1 2 3 4 5 6 7 8
        // Calculate inner product of 0, 2, 4 points
        angle = ((acce_point[2][0] - acce_point[0][0])*(acce_point[4][1]-acce_point[2][1])) - ((acce_point[2][1] - acce_point[0][1])*(acce_point[4][0] - acce_point[2][0]));
        angle += ((acce_point[3][0] - acce_point[1][0])*(acce_point[5][1]-acce_point[3][1])) - ((acce_point[3][1] - acce_point[1][1])*(acce_point[5][0] - acce_point[3][0]));
        angle += ((acce_point[4][0] - acce_point[2][0])*(acce_point[6][1]-acce_point[4][1])) - ((acce_point[4][1] - acce_point[2][1])*(acce_point[6][0] - acce_point[4][0]));
        angle += ((acce_point[5][0] - acce_point[3][0])*(acce_point[7][1]-acce_point[5][1])) - ((acce_point[5][1] - acce_point[3][1])*(acce_point[7][0] - acce_point[5][0]));
        angle += ((acce_point[6][0] - acce_point[4][0])*(acce_point[8][1]-acce_point[6][1])) - ((acce_point[6][1] - acce_point[4][1])*(acce_point[8][0] - acce_point[6][0]));
        angle += ((acce_point[3][0] - acce_point[0][0])*(acce_point[6][1]-acce_point[3][1])) - ((acce_point[3][1] - acce_point[0][1])*(acce_point[6][0] - acce_point[3][0]));
        angle += ((acce_point[4][0] - acce_point[1][0])*(acce_point[7][1]-acce_point[4][1])) - ((acce_point[4][1] - acce_point[1][1])*(acce_point[7][0] - acce_point[4][0]));
        angle += ((acce_point[5][0] - acce_point[2][0])*(acce_point[8][1]-acce_point[5][1])) - ((acce_point[5][1] - acce_point[2][1])*(acce_point[8][0] - acce_point[5][0]));
        // Calculate average for fast and slow rotation
        angle = angle / 8.0f;

        // Jump rope
        // Inner product (angle) > 0 => clockwise, < 0 => counter-clockwise, = 0 => stationary
        // Keep record
//         Log.d(TAG, "acceX_index : " + acceX_index);
        if (angle > 1.0f){
            angle_record[acce_maxDataPoints] = 2;
//            angle = 5.0f;
        }
        else if (angle < -1.0f){
            angle_record[acce_maxDataPoints] = 1;
//            angle = -5.0f;
        }
        else{
            angle_record[acce_maxDataPoints] = 0;
//            angle = 0.0f;
        }

        // Keep selected gravity in buffer
        if(acceX_index >= acce_maxDataPoints) acceX_index = 0;
        acceGet_gravity_buffer[acceX_index] = acce_gravity_filter_get;
        acceX_gravity_buffer[acceX_index] = acceX;
        acceY_gravity_buffer[acceX_index] = acceY;
        acceX_index++;

        // Calculate average (avg)
        // Calculate standard deviation (std)
        acce_avg_new = 0.0f;
        acce_avg_new_x = 0.0f;
        acce_avg_new_y = 0.0f;
        acce_std = 0.0f;
        acce_std_x = 0.0f;
        acce_std_y = 0.0f;
        angle_counter[0] = 0; angle_counter[1] = 0; angle_counter[2] = 0;
        angle_counter_top10[0] = 0; angle_counter_top10[1] = 0; angle_counter_top10[2] = 0;
        for(int i=0; i<acce_maxDataPoints; i++){
            acce_avg_new += acceGet_gravity_buffer[i];
            acce_avg_new_x += acceX_gravity_buffer[i];
            acce_avg_new_y += acceY_gravity_buffer[i];
            acce_std += (float) Math.pow(acceGet_gravity_buffer[i], 2);
            acce_std_x += (float) Math.pow(acceX_gravity_buffer[i], 2);
            acce_std_y += (float) Math.pow(acceY_gravity_buffer[i], 2);

            angle_counter[angle_record[i]]++;
            if(i > (acce_maxDataPoints-11)) angle_counter_top10[angle_record[i]]++;

            // Shift angle record
            angle_record[i] = angle_record[i+1];
        }
        acce_avg_new /= (float) acce_maxDataPoints;
        acce_avg_new_x /= (float) acce_maxDataPoints;
        acce_avg_new_y /= (float) acce_maxDataPoints;
        acce_std /= (float) acce_maxDataPoints;
        acce_std_x /= (float) acce_maxDataPoints;
        acce_std_y /= (float) acce_maxDataPoints;
        acce_std = (float) Math.sqrt( acce_std - Math.pow(acce_avg_new, 2));
        acce_std_x = (float) Math.sqrt( acce_std_x - Math.pow(acce_avg_new_x, 2));
        acce_std_y = (float) Math.sqrt( acce_std_y - Math.pow(acce_avg_new_y, 2));

        // Continue active count
        if(acce_gravity_sub > acce_gravity_sub_threshold){
            if(active_counter < active_threshold) active_counter++;
        }else{
            if(active_counter > 0) active_counter--;
        }
        // Continue active check
        // Update average
        if(active_counter > 10)
            acce_avg = acce_gravity_filter_get*SENSOR_FILER_ALPHA + (1.0f - SENSOR_FILER_ALPHA)*acce_avg;

        // Check exercise counting
        if(acce_gravity_filter_get > acce_avg_new){
            if(positive_counter < 10) positive_counter++;
        }
        else positive_counter = 0;

        if (!isUp) {
            if (acce_gravity_filter_get > acce_avg) {
                // Check exercising
                if (acce_gravity_sub > acce_gravity_sub_threshold && acce_std > acce_gravity_std_threshold) {
                    // Check the difference is grater than std or positive counting is enough
                    // Prevent jitter wave which leads to count more
                    if ((acce_gravity_filter_get - acce_avg_new) > (acce_std + 1.0f) || positive_counter > 4) {
                        temp_counter++;
                        isUp = true;
                    }
                }
            }
        } else {
            if (acce_gravity_filter_get < acce_avg_new) {
                isUp = false;
            }
        }

        // Jump rope mode
        // Check clockwise, counter-clockwoise, stationary or move up&down
        if (temp_counter > 0) {
            if (angle_counter_top10[0] > 6) {
                temp_counter = 0;
                isRotating = false;
            }
            if (angle_counter_top10[1] < 8 && angle_counter_top10[2] < 8) {
                if ((angle_counter[2] > 20 && angle_counter[2] < 40) && (angle_counter[1] > 20 && angle_counter[1] < 40)) {
                    temp_counter = 0;
                    isRotating = false;
                }
            }

            if (!isRotating) {
                // When not rotating, check it
                if (angle_counter[2] > 50) {
                    cw_counter += temp_counter;
                    temp_counter = 0;
                    isRotating = true;
                } else if (angle_counter[1] > 50) {
                    ccw_counter += temp_counter;
                    temp_counter = 0;
                    isRotating = true;
                }

                if (acce_std_x > 2.0f && acce_std_y > 2.0f) {
                    if (angle_counter[2] > angle_counter[0] || angle_counter[1] > angle_counter[0]) {
                        if (angle_counter[2] > 40) {
                            cw_counter += temp_counter;
                            temp_counter = 0;
                            isRotating = true;
                        } else if (angle_counter[1] > 40) {
                            ccw_counter += temp_counter;
                            temp_counter = 0;
                            isRotating = true;
                        }
                    }
                }

            } else {
                // When rotating, keep counting
                if (angle_counter[2] > angle_counter[1]) {
                    cw_counter += temp_counter;
                    temp_counter = 0;
                } else {
                    ccw_counter += temp_counter;
                    temp_counter = 0;
                }
            }
        }
        exercise_counter = cw_counter + ccw_counter;

        return exercise_counter;
    }

    public float getV1(){
        return this.acce_gravity_filter_get;
    }
    public float getV2(){
        return this.acce_avg;
    }
    public float getV3(){
        return this.acce_avg_new;
    }
    public int getcwCounter(){
        return this.cw_counter;
    }
    public int getccwCounter(){
        return this.ccw_counter;
    }
    public void setAccessory_mode(byte mode) {
        switch (mode){
            case 0:
                this.timestamp_threshold = 5;
                this.checkAccessoryMode = Utils.DUMBBELL;
                break;
            case 1:
                this.timestamp_threshold = 0;
                this.checkAccessoryMode = Utils.ROPE_SKIP;
                break;
            default:
                this.timestamp_threshold = 5;
                break;
        }
    }
    public void resetExerciseCounter(){
        this.exercise_counter = 0;
        this.cw_counter = 0;
        this.ccw_counter = 0;
        this.temp_counter = 0;
    }
    public double getAngle(float[] a, float[] b){
        if(a[0] == b[0] && a[1] >= b[1]) return 0;
        b[0] -= a[0];
        b[1] -= a[1];
        double angle = Math.acos(-b[1] / Math.sqrt((b[0]*b[0]) + (b[1]*b[1]))) * (180/Math.PI);

        return (b[0]<0 ? -angle : angle);
    }
    private void checkAngle(float angle_temp){
        // Inner product > 0 => clockwise, < 0 => counter-clockwise
        if (angle_temp > 0.002f){
            angle = 1.0f;
            if(angle_counter[2] < acce_maxDataPoints) angle_counter[2]++;
            if(angle_counter[1] > 0) angle_counter[1]--;
            if(angle_counter[0] > 0) angle_counter[0]--;
        }
        else if (angle_temp < -0.002f){
            angle = -1.0f;
            if(angle_counter[2] > 0) angle_counter[2]--;
            if(angle_counter[1] < acce_maxDataPoints) angle_counter[1]++;
            if(angle_counter[0] > 0) angle_counter[0]--;
        }
        else{
            angle = 0.0f;
            if(angle_counter[2] > 0) angle_counter[2]--;
            if(angle_counter[1] > 0) angle_counter[1]--;
            if(angle_counter[0] < acce_maxDataPoints) angle_counter[0]++;
        }
    }
}
