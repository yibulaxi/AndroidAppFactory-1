package com.bihe0832.android.base.debug.widget;

import com.bihe0832.android.lib.widget.BaseWidgetProvider;
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker;

public class TestWidgetProvider1 extends BaseWidgetProvider {

    @Override
    public Class<? extends BaseWidgetWorker> getWidgetWorkerClass() {
        return TestWorker1.class;
    }

    @Override
    protected boolean canAutoUpdateByOthers() {
        return true;
    }
}
