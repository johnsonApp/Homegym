package com.jht.homegym.algorithm;

import com.jht.homegym.utils.Utils;

public class AccessoryExercise {

    // Sensor raw data
    private static final float const_gravity = 9.81f;
    private float SENSOR_FILER_ALPHA = 0.2f, acceX = 0.0f, acceY = 0.0f, acceZ = 0.0f;
    private float[] acce_gravity = new float[3];
    private float[][] acce_point = new float[9][2];
    private float angle = 0.0f;
    private float acce_gravity_get = 0.0f, acce_gravity_sub = 0.0f, acce_gravity_sub_temp = 0.0f, acce_gravity_filter_x = 0.0f;
    private static final short filter_length = 5, filter_length_1 = filter_length-1;
    private float[] acceX_filter_buffer = new float[filter_length];
    private static final short acce_maxDataPoints = 60;
    private byte acceX_index = 0, index_check_pre = 0, index_check_sub = 0;
    private float[] acceX_gravity_buffer = new float[acce_maxDataPoints];
    private float acce_avg_new = 0.0f, acce_std = 0.0f, acce_avg = 0.0f;
    private byte acce_choose = 0;

    // Dumbbell counting
    private boolean isUp = false;
    private int exercise_counter = 0;
    private short active_counter = 0;

    // Jump rope counting
    private byte[] angle_record = new byte[acce_maxDataPoints];
    private byte[] angle_counter = new byte[3];
    private byte[] angle_counter_top10 = new byte[3];
    private int cw_counter = 0, ccw_counter = 0, temp_counter = 0;
    private byte checkAccessoryMode = 0;

    // Threshold
    private float acce_gravity_sub_threshold = 0.3f, acce_gravity_std_threshold = 1.0f;
    private byte active_threshold = 15, timestamp_threshold = 5;

    public AccessoryExercise(){
    }

    public AccessoryExercise(int accessoryMode){
        if(accessoryMode == Utils.DUMBBELL) {
            this.acce_gravity_sub_threshold = 0.3f;
            this.timestamp_threshold = 5;
            this.checkAccessoryMode = Utils.DUMBBELL;
        }
        else if(accessoryMode == Utils.ROPE_SKIP) {
            this.acce_gravity_sub_threshold = 1.0f;
            this.timestamp_threshold = 0;
            this.checkAccessoryMode = Utils.ROPE_SKIP;
        }
    }

    public int exerciseCounting(final float x, final float y, final float z) {
        acceX = x;
        acceY = y;
        acceZ = z;
        // Low-pass filter to reduce noise
        // to isolate the force of gravity
        acce_gravity[0] = SENSOR_FILER_ALPHA * acceX + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[0];
        acce_gravity[1] = SENSOR_FILER_ALPHA * acceY + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[1];
        acce_gravity[2] = SENSOR_FILER_ALPHA * acceZ + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[2];

        // Keep 5 points of X, Z
        for(int i=0; i<8; i++){
            acce_point[i][0] = acce_point[i+1][0];
            acce_point[i][1] = acce_point[i+1][1];
        }

        // Select X, Y for current design
        acce_point[8][0] = acce_gravity[0];
        acce_point[8][1] = acce_gravity[1];
        // Calculate inner product of 0, 2, 4 points
        angle = ((acce_point[3][0] - acce_point[0][0])*(acce_point[6][1]-acce_point[3][1])) - ((acce_point[3][1] - acce_point[0][1])*(acce_point[6][0] - acce_point[3][0]));
        angle += ((acce_point[4][0] - acce_point[1][0])*(acce_point[7][1]-acce_point[4][1])) - ((acce_point[4][1] - acce_point[1][1])*(acce_point[7][0] - acce_point[4][0]));
        angle += ((acce_point[5][0] - acce_point[2][0])*(acce_point[8][1]-acce_point[5][1])) - ((acce_point[5][1] - acce_point[2][1])*(acce_point[8][0] - acce_point[5][0]));
        angle = angle / 3.0f;
        // Inner product > 0 => clockwise, < 0 => counter-clockwise
//        if (angle > 0.002f){
//            angle = 1.0f;
//        }
//        else if (angle < -0.002f){
//            angle = -1.0f;
//        }
//        else{
//            angle = 0.0f;
//        }

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
        acce_gravity_filter_x = 0.0f;
        acce_gravity_sub = 0.0f;
        for(int i=0; i<filter_length_1; i++){
            acce_gravity_filter_x += acceX_filter_buffer[i];
            acce_gravity_sub_temp = Math.abs((acceX_filter_buffer[i+1] - acceX_filter_buffer[i]));
            if(acce_gravity_sub_temp < const_gravity)
                acce_gravity_sub += acce_gravity_sub_temp;
            acceX_filter_buffer[i] = acceX_filter_buffer[i+1];
        }
        acce_gravity_filter_x += acceX_filter_buffer[filter_length_1];
        acce_gravity_filter_x = acce_gravity_filter_x/(float) filter_length;
        acce_gravity_sub = acce_gravity_sub/(float) filter_length_1;
        acceX_filter_buffer[filter_length_1] = acce_gravity_get;

        // Keep selected gravity in buffer
        if(acceX_index >= acce_maxDataPoints) acceX_index = 0;
        acceX_gravity_buffer[acceX_index] = acce_gravity_get;

        // Jump rope
        // Inner product (angle) > 0 => clockwise, < 0 => counter-clockwise, = 0 => stationary
        // Keep record
        if (angle > 0.002f){
            angle_record[acceX_index] = 2;
            angle = 2.0f;
        }
        else if (angle < -0.002f){
            angle_record[acceX_index] = 1;
            angle = 1.0f;
        }
        else{
            angle_record[acceX_index] = 0;
            angle = 0.0f;
        }

        acceX_index++;

        // Calculate average
        // Calculate standard deviation
        acce_avg_new = 0.0f;
        acce_std = 0.0f;
        angle_counter[0] = 0; angle_counter[1] = 0; angle_counter[2] = 0;
        angle_counter_top10[0] = 0; angle_counter_top10[1] = 0; angle_counter_top10[2] = 0;
        for(int i=0; i<acce_maxDataPoints; i++){
            acce_avg_new += acceX_gravity_buffer[i];
            acce_std += (float) Math.pow(acceX_gravity_buffer[i], 2);

            angle_counter[angle_record[i]]++;
            if(i > (acce_maxDataPoints-10)) angle_counter_top10[angle_record[i]]++;
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
            acce_avg = acce_gravity_filter_x*SENSOR_FILER_ALPHA + (1.0f - SENSOR_FILER_ALPHA)*acce_avg;

        // Check exercise counting
        if (!isUp) {
            if (acce_gravity_get > acce_avg) {
                // Check X, Z exchange suddenly
                if(Math.abs(this.acce_gravity_get-this.acceX_filter_buffer[filter_length_1-1]) < const_gravity){
                    // Check exercising
                    if(acce_gravity_sub > acce_gravity_sub_threshold && acce_std > acce_gravity_std_threshold){
                        // Check counting timestamp
                        if(acceX_index < index_check_pre) index_check_sub = (byte) (acceX_index + acce_maxDataPoints - index_check_pre);
                        else index_check_sub = (byte) (acceX_index - index_check_pre);

                        // If counting time is long enough then count
                        if(index_check_sub > timestamp_threshold){
                            temp_counter++;
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

        // Jump rope mode
        // Check clockwise, counter-clockwoise, stationary or move up&down
        if(this.checkAccessoryMode == Utils.ROPE_SKIP){
            if(temp_counter > 0){
                if(angle_counter[2] > 45){
                    cw_counter += temp_counter;
                    temp_counter = 0;
                }
                if(angle_counter[1] > 45){
                    ccw_counter += temp_counter;
                    temp_counter = 0;
                }
//                if(angle_counter[0] > 50){
//                    temp_counter = 0;
//                }
                if(angle_counter_top10[0] > 8){
                    temp_counter = 0;
                }
                if(angle_counter_top10[1] < 8 && angle_counter_top10[2] < 8) {
                    if ((angle_counter[2] > 20 && angle_counter[2] < 40) && (angle_counter[1] > 20 && angle_counter[1] < 40))
                        temp_counter = 0;
                }
            }

            exercise_counter = cw_counter + ccw_counter;
        }else{
            // Dumbbell mode
            exercise_counter = temp_counter;
        }

        return exercise_counter;
    }
    public float getV1(){
        return this.acce_gravity[0]; // angle
    }
    public float getV2(){
        return this.acce_gravity[1];
    }
    public float getV3(){
        return this.acce_gravity[2];
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
