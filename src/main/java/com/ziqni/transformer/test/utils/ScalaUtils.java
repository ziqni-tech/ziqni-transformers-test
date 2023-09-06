package com.ziqni.transformer.test.utils;

import scala.collection.immutable.Seq;
import scala.jdk.javaapi.CollectionConverters;

import java.util.List;

public abstract class ScalaUtils {


    public static final scala.collection.immutable.Seq<String> emptySeqString = CollectionConverters.<String>asScala(List.of()).toSeq();

    public static <T> Seq<T> listJava2Scala(List<T> javaList){
        return scala.jdk.CollectionConverters.ListHasAsScala(javaList).asScala().toSeq();
    }
}
