package dev.javier.fitbithealth.ui.metrics

/**
 * Explicaciones breves de cada métrica para el botón de info.
 * Solo información educativa general — nunca consejo médico.
 */
data class MetricInfo(
    val title: String,
    val short: String,
    val explanation: String,
    val normalRange: String,
)

val MetricInfoMap: Map<String, MetricInfo> = mapOf(
    "rhr" to MetricInfo(
        title = "Ritmo cardíaco en reposo",
        short = "Latidos por minuto cuando estás en reposo total.",
        explanation = "Se mide mientras duermes o estás relajado. Un valor más bajo suele indicar mejor forma cardiovascular, ya que el corazón bombea con más eficiencia. El ejercicio regular, el sueño de calidad y la hidratación lo reducen con el tiempo.",
        normalRange = "60–100 lpm es el rango típico; los atletas suelen estar en 40–60.",
    ),
    "hrv" to MetricInfo(
        title = "Variabilidad del ritmo cardíaco (HRV)",
        short = "Variación de tiempo entre latidos consecutivos.",
        explanation = "Un HRV alto indica que tu sistema nervioso se adapta bien al estrés y la recuperación. Baja con estrés, mal sueño, alcohol o sobreentrenamiento. Es una de las mejores métricas para medir recuperación y fatiga.",
        normalRange = "Varía mucho por persona y edad; lo importante es tu tendencia, no el valor absoluto.",
    ),
    "spo2" to MetricInfo(
        title = "Saturación de oxígeno (SpO₂)",
        short = "Porcentaje de oxígeno en la sangre.",
        explanation = "Mide cuánta hemoglobina está transportando oxígeno. Valores bajos pueden indicar problemas respiratorios o de oxigenación. Se recomienda consultar a un médico si baja de forma sostenida por debajo del 92-94%.",
        normalRange = "95–100% es normal en personas sanas.",
    ),
    "steps" to MetricInfo(
        title = "Pasos",
        short = "Actividad diaria medida por el acelerómetro.",
        explanation = "El recuento de pasos refleja tu actividad física diaria. La OMS recomienda al menos 7.000–10.000 pasos al día para una salud cardiovascular óptima.",
        normalRange = "7.000–10.000 pasos/día es la recomendación general.",
    ),
    "sleep" to MetricInfo(
        title = "Sueño",
        short = "Duración y fases del descanso nocturno.",
        explanation = "El sueño se divide en fases: profundo (restauración física), ligero (transición) y REM (memoria y procesos mentales). Dormir 7-9 horas con suficiente fase profunda y REM es clave para la recuperación.",
        normalRange = "7–9 horas por noche; 15-25% profundo y 20-25% REM.",
    ),
    "breathing" to MetricInfo(
        title = "Ritmo respiratorio",
        short = "Respiraciones por minuto durante el sueño.",
        explanation = "Un ritmo respiratorio anormalmente alto o irregular durante el sueño puede indicar estrés, ansiedad o problemas como apnea. Se mide mejor con el sensor de respiración del dispositivo.",
        normalRange = "12–20 respiraciones/min en reposo.",
    ),
    "skin_temp" to MetricInfo(
        title = "Temperatura de la piel",
        short = "Variación térmica nocturna del cuerpo.",
        explanation = "La temperatura corporal varía en ciclos: sube ligeramente de noche. Un aumento sostenido puede indicar inicio de enfermedad, fiebre o cambios hormonales. Se interpreta mejor como tendencia.",
        normalRange = "Variación de ±0.5°C respecto a tu propia línea base.",
    ),
)

fun metricInfo(key: String): MetricInfo = MetricInfoMap[key] ?: MetricInfo(
    title = key.uppercase().replace('_', ' '),
    short = "Métrica de salud registrada por tu pulsera.",
    explanation = "Consulta la app oficial de Fitbit/Google Fit para más detalles sobre esta métrica.",
    normalRange = "Consulta tu historial para ver tu rango personal.",
)
