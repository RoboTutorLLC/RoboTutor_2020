/*****************************************************************************************
* Copyright (c) 2007 Hewlett-Packard Development Company, L.P.
* Permission is hereby granted, free of charge, to any person obtaining a copy of
* this software and associated documentation files (the "Software"), to deal in
* the Software without restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
* Software, and to permit persons to whom the Software is furnished to do so,
* subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
* PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*****************************************************************************************/

/************************************************************************
 * SVN MACROS
 *
 * $LastChangedDate: 2008-07-04 11:43:39 +0530 (Fri, 04 Jul 2008) $
 * $Revision: 544 $
 * $Author: sharmnid $
 *
 ************************************************************************/

/************************************************************************
 * FILE DESCR: Implementation of the String Splitter Module
 *
 * CONTENTS:
 *	tokenizeString
 *
 * AUTHOR:     Balaji R.
 *
 * DATE:       December 23, 2004
 * CHANGE HISTORY:
 * Author		Date			Description of change
 ************************************************************************/

#include "LTKLoggerUtil.h"
#include "LTKOSUtil.h"
#include "LTKOSUtilFactory.h"
#include "LTKLogger.h"
#include "LTKMacros.h"
#include "LTKErrors.h"
#include "LTKErrorsList.h"
#include "logger.h"
#include <android/log.h>

#ifdef _WIN32
#include <windows.h>
#endif

// define to enable android logging
//#define ANDROID_LOG

void* LTKLoggerUtil::m_libHandleLogger = NULL;
LTKOSUtil* LTKLoggerUtil::m_ptrOSUtil = NULL;
FN_PTR_LOGMESSAGE LTKLoggerUtil::module_logMessage = NULL;
FN_PTR_STARTLOG LTKLoggerUtil::module_startLogger = NULL;
FN_PTR_GETINSTANCE LTKLoggerUtil::module_getInstanceLogger = NULL;
FN_PTR_DESTROYINSTANCE LTKLoggerUtil::module_destroyLogger = NULL;
ofstream LTKLoggerUtil::m_emptyStream;

/****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			: 09-Jul-2007
* NAME			: LTKLoggerUtil
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
****************************************************************************/

LTKLoggerUtil::LTKLoggerUtil(){}



/****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			: 09-Jul-2007
* NAME			: createLogger
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
****************************************************************************/

int LTKLoggerUtil::createLogger(const string& lipiRoot)
{
#if 0
	/* Android port : commenting the load of shared object and also mapping of functions
	 * as there is only one shared object.
	 */

    void* functionHandle = NULL;
	m_ptrOSUtil = LTKOSUtilFactory::getInstance();

    int iErrorCode = m_ptrOSUtil->loadSharedLib(lipiRoot,
                                                LOGGER_MODULE_STR,
                                                &m_libHandleLogger);


    if(iErrorCode != SUCCESS)
    {
		delete m_ptrOSUtil;
        return iErrorCode;
    }

    // Create logger instance
    if (module_getInstanceLogger == NULL)
    {
        iErrorCode = m_ptrOSUtil->getFunctionAddress(m_libHandleLogger,
                                                     "getLoggerInstance",
                                                     &functionHandle);
        if(iErrorCode != SUCCESS)
    	{
			delete m_ptrOSUtil;
    	    return iErrorCode;
    	}

        module_getInstanceLogger = (FN_PTR_GETINSTANCE)functionHandle;

    	functionHandle = NULL;
    }

    module_getInstanceLogger();

    // map destoylogger function
    if (module_destroyLogger == NULL)
    {
        iErrorCode = m_ptrOSUtil->getFunctionAddress(m_libHandleLogger,
                                                     "destroyLogger",
                                                     &functionHandle);
        if(iErrorCode != SUCCESS)
    	{
			delete m_ptrOSUtil;
    	    return iErrorCode;
    	}

        module_destroyLogger = (FN_PTR_DESTROYINSTANCE)functionHandle;

    	functionHandle = NULL;
    }

	delete m_ptrOSUtil;
#endif

    return SUCCESS;

}

/*****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			:
* NAME			: destroyLogger
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
*****************************************************************************/
int LTKLoggerUtil::destroyLogger()
{
	m_ptrOSUtil = LTKOSUtilFactory::getInstance();

    if (module_destroyLogger != NULL )
    {
        module_destroyLogger();
    }

	int returnVal = m_ptrOSUtil->unloadSharedLib(m_libHandleLogger);

	delete m_ptrOSUtil;
    return returnVal;
}


/*****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			:
* NAME			: getAddressLoggerFunctions
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
*****************************************************************************/
int LTKLoggerUtil::configureLogger(const string& logFile, LTKLogger::EDebugLevel logLevel)
{
#if 0
	/* Android port: commenting the load of shared objects and mapping of functions
	 * as there is only one shared object.
	 */

     void* functionHandle = NULL;
     int returnVal = SUCCESS;

     FN_PTR_SETLOGFILENAME module_setLogFileName = NULL;
     FN_PTR_SETLOGLEVEL module_setLogLevel = NULL;

    if (m_libHandleLogger == NULL )
    {
        LTKReturnError(ELOGGER_LIBRARY_NOT_LOADED);
    }

    m_ptrOSUtil = LTKOSUtilFactory::getInstance();

    if ( logFile.length() != 0 )
    {
        returnVal = m_ptrOSUtil->getFunctionAddress(m_libHandleLogger,
                                                    "setLoggerFileName",
                                                    &functionHandle);

        if(returnVal != SUCCESS)
    	{
    	    return returnVal;
    	}

        module_setLogFileName = (FN_PTR_SETLOGFILENAME)functionHandle;

    	functionHandle = NULL;

        module_setLogFileName(logFile);

    }
    else
    {
		LTKReturnError(EINVALID_LOG_FILENAME);
    }

    returnVal = m_ptrOSUtil->getFunctionAddress(m_libHandleLogger,
                                                "setLoggerLevel",
                                                &functionHandle);

    if(returnVal != SUCCESS)
	{
	    LTKReturnError(returnVal);
	}

    module_setLogLevel = (FN_PTR_SETLOGLEVEL)functionHandle;

	functionHandle = NULL;

    module_setLogLevel(logLevel);

	delete m_ptrOSUtil;
#endif

	/* Android port : get an instance of logger.
	 */
	LTKLoggerInterface* logger = getLoggerInstance();
	logger->setLogLevel(logLevel);

    return SUCCESS;

}


/*****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			:
* NAME			: getAddressLoggerFunctions
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
*****************************************************************************/
int LTKLoggerUtil::getAddressLoggerFunctions()
{
    void* functionHandle = NULL;
    int returnVal = SUCCESS;


    //start log

    if (module_startLogger == NULL )
    {
        returnVal = m_ptrOSUtil->getFunctionAddress(m_libHandleLogger,
                                                    "startLogger",
                                                    &functionHandle);

        if(returnVal != SUCCESS)
    	{
    	    LTKReturnError(returnVal);
    	}

        module_startLogger = (FN_PTR_STARTLOG)functionHandle;

    	functionHandle = NULL;
    }

    module_startLogger();

    // map Log message
    if (module_logMessage == NULL)
    {
        returnVal = m_ptrOSUtil->getFunctionAddress(m_libHandleLogger,
                                                    "logMessage",
                                                    &functionHandle);

        if(returnVal != SUCCESS)
    	{
    	    LTKReturnError(returnVal);
    	}

        module_logMessage = (FN_PTR_LOGMESSAGE)functionHandle;

    	functionHandle = NULL;

    }


	return SUCCESS;

}

/*****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			: 15-Jul-2008
* NAME			: nidhi
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
*****************************************************************************/


ostream& LTKLoggerUtil::logMessage(LTKLogger::EDebugLevel logLevel, string inStr, int lineNumber)
{
#if 0
	/* Android port : commenting the load of shared objects and also mapping of functions
	 * as there is only one shared object.
	 */

	m_ptrOSUtil = LTKOSUtilFactory::getInstance();

	if (m_libHandleLogger == NULL)
	{
		m_libHandleLogger = m_ptrOSUtil->getLibraryHandle(LOGGER_MODULE_STR);

		if (m_libHandleLogger == NULL)
		{
			delete m_ptrOSUtil;
			return m_emptyStream;
		}
	}


	// get function addresses
    if ( module_startLogger == NULL ||
        module_logMessage == NULL )
    {
        int returnVal = getAddressLoggerFunctions();

        if(returnVal != SUCCESS)
    	{
			delete m_ptrOSUtil;
    	    return m_emptyStream;
    	}
    }

	delete m_ptrOSUtil;
	return module_logMessage(logLevel, inStr, lineNumber);
#endif

#define APPNAME "LIPITK_NATIVE"

#ifdef ANDROID_LOG
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "%s : %d", inStr.c_str(), lineNumber );
#endif // #ifdef ANDROID_LOG

	/* Android port :  get an instance of logger instance and invoke overloaded operator()
	 * from LTKLogger.cpp. As logger is implemented as a singleton class, only one instance
	 * is returned irrespective of how many times getLoggerInstance() is called.
	 */
	LTKLoggerInterface* logger = getLoggerInstance();
	return (*logger)(logLevel, inStr, lineNumber);

}



/*****************************************************************************
* AUTHOR		: Nidhi Sharma
* DATE			: 15-Jul-2008
* NAME			: nidhi
* DESCRIPTION	:
* ARGUMENTS		:
* RETURNS		:
* NOTES			:
* CHANGE HISTROY
* Author			Date				Description of change
*****************************************************************************/


void LTKLoggerUtil::AlogMessage(LTKLogger::EDebugLevel logLevel, string msg, string inStr, int lineNumber)
{
    /* Android port :  get an instance of logger instance and invoke overloaded operator()
     * from LTKLogger.cpp. As logger is implemented as a singleton class, only one instance
     * is returned irrespective of how many times getLoggerInstance() is called.
     */
#define APPNAME "LIPITK_NATIVE"

#ifdef ANDROID_LOG
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "%s | %s : %d", msg.c_str(), inStr.c_str(), lineNumber );
#endif  // #ifdef ANDROID_LOG

}
