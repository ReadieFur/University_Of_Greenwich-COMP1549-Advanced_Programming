package readiefur.helpers;

import java.util.concurrent.TimeoutException;

public class ManualResetEvent
{
    private boolean isSet = false;
    private Object lock = new Object();

    public ManualResetEvent(boolean initialState)
    {
        if (initialState)
            Set();
    }

    public Boolean IsSet()
    {
        synchronized (lock)
        {
            return isSet;
        }
    }

    public void Set()
    {
        synchronized (lock)
        {
            isSet = true;
            lock.notifyAll();
        }
    }

    public void Reset()
    {
        synchronized (lock)
        {
            isSet = false;
        }
    }

    public void WaitOne() throws TimeoutException
    {
        WaitOneInternal(-1);
    }

    //Throws InterruptedException if the timeout occurs.
    public void WaitOne(long timeoutMilliseconds) throws TimeoutException
    {
        WaitOneInternal(timeoutMilliseconds);
    }

    private void WaitOneInternal(long timeoutMilliseconds) throws TimeoutException
    {
        synchronized (lock)
        {
            while (!isSet)
            {
                try
                {
                    if (timeoutMilliseconds >= 0)
                    {
                        long startTime = System.currentTimeMillis();
                        lock.wait(timeoutMilliseconds);
                        if (System.currentTimeMillis() - startTime >= timeoutMilliseconds)
                            throw new TimeoutException();
                    }
                    else
                    {
                        lock.wait();
                    }
                }
                catch (InterruptedException e)
                {
                    //I should do a check here to see if we were interrupted by a notification but it should be ok without for now.
                }
            }
        }
    }
}
