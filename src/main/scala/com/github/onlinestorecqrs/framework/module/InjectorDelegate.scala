package com.github.onlinestorecqrs.framework.module

import com.google.inject.Injector

class InjectorDelegate(injector: Injector) {
    def getInstance[T](clazz: Class[T]) = {
        injector.getInstance(clazz)
    }
}
