package com.jht.homegym.algorithm;

public class AccessoryExercise {

    // Sensor raw data
    private static final float const_gravity = 9.81f;
    private float SENSOR_FILER_ALPHA = 0.2f, acceX = 0.0f, acceY = 0.0f, acceZ = 0.0f;
    private float[] acce_gravity = new float[3], acce_linear = new float[3];
    private float acce_gravity_get = 0.0f, acce_gravity_sub = 0.0f, acce_gravity_sub_temp = 0.0f, acce_gravity_filter_x = 0.0f;
    private static final short filter_length = 5, filter_length_1 = filter_length-1;
    private float[] acceX_filter_buffer = new float[filter_length];
    private static final short acce_maxDataPoints = 60;
    private byte acceX_index = 0, index_check_pre = 0, index_check_sub = 0;
    private float[] acceX_gravity_buffer = new float[acce_maxDataPoints];
    private float acce_avg_new = 0.0f, acce_std = 0.0f, acce_avg = 0.0f;

    // Dumbbell counting
    private boolean isUp = false, isGravityX = false;
    private int exercise_counter = 0;
    private short active_counter = 0;
    // Threshold
    private float acce_gravity_sub_threshold = 0.3f, acce_gravity_std_threshold = 1.0f;
    private byte active_threshold = 15, timestamp_threshold = 5;

    public int exerciseCounting(final float x, final float y, final float z) {
        // Normalize to 0.0f~9.81f
        acceX = x/0.032f * const_gravity;
        //acceY = y/0.032f * const_gravity;
        acceZ = z/0.032f * const_gravity;
        // Low-pass filter to reduce noise
        // to isolate the force of gravity
        acce_gravity[0] = SENSOR_FILER_ALPHA * acceX + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[0];
        //acce_gravity[1] = SENSOR_FILER_ALPHA * acceY + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[1];
        acce_gravity[2] = SENSOR_FILER_ALPHA * acceZ + (1.0f - SENSOR_FILER_ALPHA) * acce_gravity[2];
        acce_linear[0] = acceX - acce_gravity[0];
        acce_linear[2] = acceZ - acce_gravity[2];

        // Check to get gravity x or gravity z
        if(active_counter < 10) isGravityX = (Math.abs(acce_gravity[0]) > Math.abs(acce_gravity[2]))? true : false;
        acce_gravity_get = (isGravityX)? acce_gravity[0]:acce_gravity[2];

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

        // Keep gravity x in buffer
        if(acceX_index >= acce_maxDataPoints) acceX_index = 0;
        acceX_gravity_buffer[acceX_index] = acce_gravity_get;
        acceX_index++;

        // Calculate average
        // Calculate standard deviation
        acce_avg_new = 0.0f;
        acce_std = 0.0f;
        for(int i=0; i<acce_maxDataPoints; i++){
            acce_avg_new += acceX_gravity_buffer[i];
            acce_std += (float) Math.pow(acceX_gravity_buffer[i], 2);
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
    public float getV1(){
        return this.acce_gravity_get;
    }
    public float getV2(){
        return this.acce_linear[0];
    }
    public float getV3(){
        return this.acce_std;
    }
    public void setAccessory_mode(byte mode) {
        switch (mode){
            case 0:
                this.timestamp_threshold = 5;
                break;
            case 1:
                this.timestamp_threshold = 0;
                break;
            default:
                this.timestamp_threshold = 5;
                break;
        }
    }
    public void resetExerciseCounter(){
        this.exercise_counter = 0;
    }
}
