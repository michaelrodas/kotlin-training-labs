package org.course.utils

import org.slf4j.LoggerFactory


inline val <reified T>  T.logger: org.slf4j.Logger
    get() = LoggerFactory.getLogger(T::class.java)
