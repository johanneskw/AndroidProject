package com.example.gruppe30in2000.StationUtil

class AQILevel{

    companion object {
        
        //Dette er riktig verdier:
        //val low = 2.0
        //val medium = 3.0
        
        val low = 2.0
        val medium = 3.0

        fun getAQILevel(aqiValue : Double) : Int {
            // Low = 1, Med =, High = 3
            if (aqiValue < low) {
                return 1
            } else if (aqiValue >= low && aqiValue < medium) {
                return 2
            } else {
                return 3
            }
        }

        fun getAQILevelString(aqiValue : Double) : String {
            // Low = 1, Med =, High = 3
            if (aqiValue < low) {
                return "Lav"
            } else if (aqiValue >= low && aqiValue < medium) {
                    return "Moderat"
                } else {
                return "Hoy"
            }
        }


        fun getAlertLevel(aqiValue : Double) : Int {
            if (aqiValue < low) {
                return 0
            } else if (aqiValue >= low && aqiValue < medium) {
                return 1
            } else {
                return 2
            }
        }
    }

}

