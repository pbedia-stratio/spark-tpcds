package com.stratio

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.spark.sql.SparkSession

object PerfMain{

  def main(args: Array[String]): Unit = {

    val hdfsUrl = args.head
    val queriesPath = args.tail.head
    val queriesToExecute = args.tail.tail

    val content = scala.io.Source.fromURL(queriesPath).getLines().mkString("\n")
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val json = mapper.readValue(content, classOf[Queries])

    implicit val spark = SparkSession
      .builder()
      .appName("TCP-DS Queries")
      .getOrCreate()

    val tables = Seq("call_center",
      "catalog_page",
      "catalog_returns",
      "catalog_sales",
      "customer",
      "customer_address",
      "customer_demographics",
      "date_dim",
      "household_demographics",
      "income_band",
      "inventory",
      "item",
      "promotion",
      "reason",
      "ship_mode",
      "store",
      "store_returns",
      "time_dim",
      "warehouse",
      "web_page",
      "web_returns",
      "web_sales",
      "web_site",
      "store_sales")

    tables.foreach(table => spark.read.parquet( hdfsUrl + s"/$table").createOrReplaceTempView(table))

    json
      .queries
        .filter(query => {
          queriesToExecute.contains(query.name)
        }).foreach( query => {
        LaunchPad.executeQuery(query.name, query.sql).foreach(
          println)
      }

      )

  }

}
case class Queries(queries: List[Query]){}
case class Query(name: String, sql: String){}
