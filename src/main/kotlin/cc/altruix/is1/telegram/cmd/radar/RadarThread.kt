package cc.altruix.is1.telegram.cmd.radar

import cc.altruix.is1.App
import cc.altruix.is1.mongo.IAltruixIs1MongoSubsystem
import cc.altruix.is1.telegram.IResponsiveBot
import cc.altruix.is1.telegram.ITelegramUtils
import cc.altruix.is1.telegram.cmd.radar.metrics.*
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.data.category.DefaultCategoryDataset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.api.methods.send.SendPhoto
import java.io.File
import java.time.LocalDate


/**
 * Created by pisarenko on 16.05.2017.
 */
open class RadarThread(
        val mongo: IAltruixIs1MongoSubsystem,
        val bot: IResponsiveBot,
        val chatId: Long,
        val tu: ITelegramUtils,
        val logger: Logger = LoggerFactory.getLogger(App.LoggerName)
) : Thread() {
    companion object {
        val TargetsAbs = RadarChartData(
                mapOf(
                        TotalProductiveTime to 40.0,
                        VocabularyLearningSessions to 5.0,

                        // TODO: Enable ReadingProgressLoc metric
                        // ReadingProgressLoc to 300.0,

                        // TODO: Enable ReadingProgressPages metric
                        // ReadingProgressPages to 20.0,

                        ShortStoriesRead to 3.0,
                        ShortStoriesPublished to 1.0,
                        MoviesWatched to 1.0,
                        TimeSpentOnScreenWritingMarketing to 1.0,
                        TimeSpentOnSvWorldbuilding to 1.0,
                        EveningTeethBrushings to 7.0,
                        WritingExercisesDone to 1.0,
                        CafeVisits to 1.0,
                        MarketingExperimentsConducted to 1.0
                )
        )
    }
    override fun run() {
        val now = now()
        val dataCreator = RadarDataCreator(mongo)
        val actualAbsRes = dataCreator.calculateRadarData(now)
        val actualAbs = actualAbsRes.result
        if (!actualAbsRes.success || (actualAbs == null)) {
            tu.displayError("Could not get data ('${actualAbsRes.error}').", chatId, bot)
            return
        }
        val actualPercent:RadarChartData = calculatePercent(TargetsAbs, actualAbs)
        val sortedMetrics = TargetsAbs.amountsByMetric.keys
                .sortedBy { metric -> metric.sortOrder }
        val dataSet = composeDataSet(sortedMetrics, actualPercent)
        val caption = composeCaption(sortedMetrics)
        val plot = createPlot(dataSet)
        val chart = JFreeChart(plot)

        var fileToUpload: File? = null
        try {
            fileToUpload = File.createTempFile("Altruix_IS_1_RadarThread", ".tmp")
            saveChartAsPNG(fileToUpload, chart)
            val sendPhoto = createSendPhoto(fileToUpload, "Radar chart")
            bot.sendImage(sendPhoto)
        }
        catch (t:Throwable) {
            logger.error("run", t)
            tu.displayError("Could not create the chart.", chatId, bot)
            return
        }
        finally {
            fileToUpload?.delete()
            fileToUpload?.deleteOnExit()
        }
        tu.sendTextMessage(caption, chatId, bot)
        mongo.saveRadarData(now, TargetsAbs, actualAbs)
    }

    private fun saveChartAsPNG(fileToUpload: File?, chart: JFreeChart) {
        ChartUtilities.saveChartAsPNG(
                fileToUpload,
                chart,
                400,
                400)
    }

    private fun createSendPhoto(fileToUpload: File?, caption: String): SendPhoto {
        val sendPhoto = SendPhoto()
        sendPhoto.chatId = chatId.toString()
        sendPhoto.setNewPhoto(fileToUpload)
        sendPhoto.caption = caption
        return sendPhoto
    }

    protected fun createPlot(dataSet: DefaultCategoryDataset): SpiderWebPlot2 {
        val plot = SpiderWebPlot2(dataSet)
        for (i: Int in 0..(dataSet.columnCount - 1)) {
            plot.setOrigin(i, 0.0)
            plot.setMaxValue(i, 100.0)
        }
        plot.isAxisTickVisible = true
        return plot
    }

    protected fun composeDataSet(
            sortedMetrics: List<RadarChartMetric>,
            actualPercent: RadarChartData): DefaultCategoryDataset {
        val dataSet = DefaultCategoryDataset()
        sortedMetrics
                .forEach { metric ->
                    insertActualDataPercentPair(actualPercent, metric, dataSet);
                }
        return dataSet
    }

    open fun insertActualDataPercentPair(
            actualPercent: RadarChartData,
            metric: RadarChartMetric,
            dataSet: DefaultCategoryDataset) {
        val actual = capAt100Percent(actualPercent, metric)
        val target = 100.0
        val abbr = metric.abbr
        dataSet.addValue(actual, "Actual", abbr);
        dataSet.addValue(target, "Target", abbr)
    }

    open fun composeCaption(sortedMetrics: List<RadarChartMetric>): String {
        val caption = StringBuilder()
        caption.append("Radar chart")
        sortedMetrics
                .forEach { metric ->
                    val abbr = metric.abbr
                    caption.append(", $abbr = ${metric.name} [${metric.unit}]")
                }
        return caption.toString()
    }

    open fun now() = LocalDate.now()

    open fun calculatePercent(
            targetsAbs: RadarChartData, 
            actualAbs: RadarChartData
    ): RadarChartData {
        val res = HashMap<RadarChartMetric,Double>()
        actualAbs.amountsByMetric.keys.forEach { metric ->
            val targetAmt = targetsAbs.amountsByMetric[metric]
            val actualAmt = actualAbs.amountsByMetric[metric]
            if ((targetAmt != null) && (actualAmt != null)) {
                res[metric] = (actualAmt*100.0) / targetAmt
            }
        }
        return RadarChartData(res)
    }

    open fun capAt100Percent(
            actualPercent: RadarChartData,
            metric: RadarChartMetric
    ):Double {
        val orig = actualPercent.amountsByMetric[metric] ?: 0.0
        if (orig > 100.0) {
            return 100.0
        }
        return orig
    }
}