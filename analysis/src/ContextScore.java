public class ContextScore {


    private int requestUsageContextBase = 20;
    private int usageContextBase = 10;
    private int backgroundContextBase = 10;
    private int backgroundForegroundContextBase = 5;
    private int requestContextBase = 5;

    private int requestUsageContextMax = 40;
    private int usageContextMax = 20;
    private int backgroundContextMax = 20;
    private int backgroundForegroundContextMax = 10;
    private int requestContextMax = 10;

    public float getRequestUsageContext() {
        return requestUsageContext;
    }

    public void setRequestUsageContext() {
        if(this.requestUsageContext == 0) {
            this.requestUsageContext = requestUsageContextBase;
        } else {
            this.requestUsageContext+=4;
            this.requestUsageContext = getScaledScore(requestUsageContextBase, requestUsageContextMax, this.requestUsageContext);
        }
        setFinalScore();
    }

    public float getUsageContext() {
        return usageContext;
    }

    public void setUsageContext() {
        if(this.usageContext == 0) {
            this.usageContext = usageContextBase;
        } else {
            this.usageContext += 2;
            this.usageContext = getScaledScore(usageContextBase, usageContextMax, this.usageContext);
        }
        setFinalScore();
    }

    public float getBackgroundContext() {
        return backgroundContext;
    }

    public void setBackgroundContext() {
        if(this.backgroundContext == 0) {
            this.backgroundContext = usageContextBase;
        } else {
            this.backgroundContext += 2;
            this.backgroundContext = getScaledScore(backgroundContextBase, backgroundContextMax, this.backgroundContext);
        }
        setFinalScore();
    }

    public float getBackgroundForegroundContext() {
        return backgroundForegroundContext;
    }

    public void setBackgroundForegroundContext() {
        if(this.backgroundForegroundContext == 0) {
            this.backgroundForegroundContext = backgroundForegroundContextBase;
        } else {
            this.backgroundForegroundContext += 2;
            this.backgroundForegroundContext = getScaledScore(backgroundForegroundContextBase, backgroundForegroundContextMax, this.backgroundForegroundContext);
        }
        setFinalScore();
    }

    public float getRequestContext() {
        return requestContext;
    }

    public void setRequestContext() {
        if(this.requestContext == 0) {
            this.requestContext = requestContextBase;
        } else {
            this.requestContext += 2;
            this.requestContext = getScaledScore(requestContextBase, requestContextMax, this.requestContextBase);
        }
        setFinalScore();
    }

    public float getFinalScore() {
        return finalScore;
    }

    public void setFinalScore() {
        this.finalScore = usageContext + requestContext + requestUsageContext + backgroundContext + backgroundForegroundContext;
    }

    private float getScaledScore(int base, int max, float score) {
        return (float) (max * (2/3.1416) * Math.atan(score - base));
    }

    private float requestUsageContext;
    private float usageContext;
    private float backgroundContext;
    private float backgroundForegroundContext;
    private float requestContext;
    private float finalScore;

    public void increaseRequestUsageContextContextScore(int count) {
        for (int i = 0; i < count; i++) {
            setRequestUsageContext();
        }
    }

    public void increaseRequestContextScore(int count) {
        for (int i = 0; i < count; i++) {
            setRequestContext();
        }
    }

    public void increaseUsageContextScore(int count) {
        for (int i = 0; i < count; i++) {
            setUsageContext();
        }
    }


    public void increaseBackGroundContextScore(int count) {
        for (int i = 0; i < count; i++) {
            setBackgroundContext();
        }
    }

    public void increaseBackGroundForeGroundContextScore(int count) {
        for (int i = 0; i < count; i++) {
            setBackgroundForegroundContext();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(this.finalScore);
    }
}
