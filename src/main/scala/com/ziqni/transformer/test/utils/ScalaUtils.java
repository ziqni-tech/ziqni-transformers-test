package com.ziqni.transformer.test.utils;

import scala.jdk.javaapi.CollectionConverters;

import java.util.List;

public abstract class ScalaUtils {


    public static final scala.collection.immutable.Seq<String> emptySeqString = CollectionConverters.<String>asScala(List.of()).toSeq();
}
