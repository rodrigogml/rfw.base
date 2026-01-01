package br.eng.rodrigogml.rfw.base.jobmonitor.interfaces;

import br.eng.rodrigogml.rfw.base.jobmonitor.JobMonitor;
import br.eng.rodrigogml.rfw.base.jobmonitor.JobStatus;
import br.eng.rodrigogml.rfw.kernel.exceptions.RFWException;

/**
 * Description: Implementação padrão (para JobMonitor dentro da mesma JVM) da {@link JobStatusSupplierDefault}.<br>
 *
 * @author Rodrigo GML
 * @since 10.0 (31 de out de 2020)
 */
public class JobStatusSupplierDefault implements JobStatusSupplier {

  @Override
  public JobStatus getJobStatus(String jobUUID) throws RFWException {
    return JobMonitor.getJobStatus(jobUUID);
  }

  @Override
  public boolean cleanJob(String jobUUID) throws RFWException {
    return JobMonitor.cleanJob(jobUUID);
  }

  @Override
  public void interrupt(String jobUUID) {
    JobMonitor.interrupt(jobUUID);
  }

  @Override
  public void interrupt(String jobUUID, RFWException ex) {
    JobMonitor.interrupt(jobUUID, ex);
  }

}
