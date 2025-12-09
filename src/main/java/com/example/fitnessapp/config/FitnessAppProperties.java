package com.example.fitnessapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fitnessapp")
public class FitnessAppProperties {

    private Data data = new Data();
    private Microservice microservice = new Microservice();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Microservice getMicroservice() {
        return microservice;
    }

    public void setMicroservice(Microservice microservice) {
        this.microservice = microservice;
    }

    public static class Data {
        private boolean initialize = true;

        public boolean isInitialize() {
            return initialize;
        }

        public void setInitialize(boolean initialize) {
            this.initialize = initialize;
        }
    }

    public static class Microservice {
        private Food food = new Food();

        public Food getFood() {
            return food;
        }

        public void setFood(Food food) {
            this.food = food;
        }

        public static class Food {
            private String url = "http://localhost:8081";

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}

