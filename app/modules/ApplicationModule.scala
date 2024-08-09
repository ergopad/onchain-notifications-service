package modules

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder

import org.reflections.Reflections

import processor.plugins.EventProcessorPlugin
import processor.plugins.DynamicConfigPlugin

class ApplicationModule extends AbstractModule {
  override def configure(): Unit = {
    import scala.collection.JavaConverters._
    val r = new Reflections("processor.plugins")

    // bind event processor plugins
    val subtypesEventProcessorPlugin =
      r.getSubTypesOf(classOf[EventProcessorPlugin])
    val executorBinderEventProcessorPlugin =
      Multibinder.newSetBinder(binder(), classOf[EventProcessorPlugin])
    subtypesEventProcessorPlugin.asScala.foreach { clazz =>
      executorBinderEventProcessorPlugin.addBinding().to(clazz)
    }

    // bind dynamic config plugins
    val subtypesDynamicConfigPlugin =
      r.getSubTypesOf(classOf[DynamicConfigPlugin])
    val executorBinderDynamicConfigPlugin =
      Multibinder.newSetBinder(binder(), classOf[DynamicConfigPlugin])
    subtypesDynamicConfigPlugin.asScala.foreach { clazz =>
      executorBinderDynamicConfigPlugin.addBinding().to(clazz)
    }
  }
}
