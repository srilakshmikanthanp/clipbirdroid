package com.srilakshmikanthanp.clipbirdroid.interfaces

interface ClipboardChangeListener {
  fun onClipboardChange(content: MutableList<Pair<String, String>>)
}
