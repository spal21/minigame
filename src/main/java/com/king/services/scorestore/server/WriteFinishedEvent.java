package com.king.services.scorestore.server;

class WriteFinishedEvent extends BasicEvent {
    WriteFinishedEvent(BasicExchangeImpl var1) {
        super(var1);

        assert !var1.writefinished;

        var1.writefinished = true;
    }
}