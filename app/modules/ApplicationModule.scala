package modules

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder

import org.reflections.Reflections

import processor.plugins.EventProcessorPlugin

class ApplicationModule extends AbstractModule {
  override def configure(): Unit = {
    import scala.collection.JavaConverters._
    val r = new Reflections("processor.plugin")
    val subtypes = r.getSubTypesOf(classOf[EventProcessorPlugin])

    val executorBinder =
      Multibinder.newSetBinder(binder(), classOf[EventProcessorPlugin])
    subtypes.asScala.foreach { clazz =>
      executorBinder.addBinding().to(clazz)
    }
  }
}
