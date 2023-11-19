FROM hypi/fenrir-runtime-java:v1

ADD target/fn-google-analytics-*.jar /home/hypi/fenrir/function/fn-google-analytics.jar
ADD target/lib/* /home/hypi/fenrir/function
