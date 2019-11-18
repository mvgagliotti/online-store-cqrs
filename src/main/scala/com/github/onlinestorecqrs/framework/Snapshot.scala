package com.github.onlinestorecqrs.framework

case class Snapshot[R](payload: R)