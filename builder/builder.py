import click
import os
import sys
import logging
from typing import Dict
import urllib.parse as parse

TIMESTAMP_FORMAT = '%Y-%m-%dT%H:%M:%S'
SHORT_TIMESTAMP_FORMAT = '%Y-%m-%d'

logger:object = logging.getLogger('Artifact Builder ===>')
logging.basicConfig(stream=sys.stdout, level=logging.DEBUG)
class builder:
    def __init__(self, *args, **kwargs) -> None:
        print('CMR Query object created')

    def find_executable(self, executable, path=None):
        """Tries to find 'executable' in the directories listed in 'path'.

        A string listing directories separated by 'os.pathsep'; defaults to
        os.environ['PATH'].  Returns the complete filename or None if not found.
        """
        if path is None:
            path = os.environ.get('PATH', os.defpath)

        paths = path.split(os.pathsep)
        base, ext = os.path.splitext(executable)

        if (sys.platform == 'win32' or os.name == 'os2') and (ext != '.exe'):
            executable = executable + '.exe'

        if not os.path.isfile(executable):
            for p in paths:
                f = os.path.join(p, executable)
                if os.path.isfile(f):
                    # the file exists, we have a shot at spawn working
                    return f
            return None
        else:
            return executable

    def clean_up(self, project_dir:str):
        target_dir = os.path.join(project_dir, 'target')
        build_dir = os.path.join(project_dir, 'build')
        os.system('rm -Rf {}'.format(target_dir))
        os.system('rm -Rf {}'.format(build_dir))

@click.command()
@click.option('-d', '--project-dir',
              help='password for git', required=True, type=str)
@click.option('-a', '--artifact-base-name',
              help='artifact base name without .zip. Ex. cnmToGranule', required=True, type=str)
def process(project_dir:str, artifact_base_name:str) -> None:
    '''
        TODOS
        * More logging , perhaps using logging package
        * check -SNAPSHOT string before builder
        check mvn test output has test complete
        move build/distributions/xxxx.zip to the ~/releases directory

        Modify pom <version>, tag, commit, push

    '''
    logger.debug('project directory:{}'.format('project_dir'))
    logger.debug('artifact name: {}'.format(artifact_base_name))
    builder_o = builder()
    mvn_executable:str = builder_o.find_executable('mvn')
    gradle_executable:str = builder_o.find_executable('gradle')
    logger.debug('maven executable: {}'.format(mvn_executable))
    logger.debug('gradle executable:{}'.format(gradle_executable))
    os.system('pwd')
    os.chdir(project_dir)
    os.system('pwd')
    # zip and tar.gz the source before build
    builder_o.clean_up(project_dir)
    os.system('zip -r /tmp/source.zip . -x builder/\\* cache/\\* gradle/\\* release/\\* target/\\* build/\\* .git/\\*')
    os.system('tar -cvf /tmp/source.tar.gz --exclude builder --exclude build --exclude target --exclude release --exclude cache --exclude gradle --exclude .git .')
    '''
        Read maven pom.xml <version>
    '''
    stream_pom_version = os.popen('mvn help:evaluate -Dexpression=project.version -q -DforceStdout')
    pom_version: str = stream_pom_version.read()
    logger.debug('Read version from pom.xml:{}'.format(pom_version))
    if pom_version.lower().find('snapshot') == -1:
        logger.info('There is no SNAPSHOT in pom version. Stopping build ...')
        exit(0)
    else:
        release_version:str = pom_version.lower().replace('-snapshot','')
        logger.info('After removing SNAPSHOT, release version:{}'.format(release_version))

    # Maven clean and TEST
    # find if TEST has Failue case equals 0 then advance otherwise, exit the program
    os.system('{} clean'.format(mvn_executable))
    stream_mvn_test = os.popen('{} test'.format(mvn_executable))
    str_test_result: str = stream_mvn_test.read()
    logger.debug('Entire MVN TEST output: {}'.format(str_test_result))
    if str_test_result.find('Failures: 0') != -1:
        logger.info('MAVEN TEST SUCCEEDED.  Continue ...')
    else:
        logger.error('MAVEN TEST FAILURE.  Existing ...')
        exit(1)
    # Build artifact using maven and gradle commands
    os.system('mvn dependency:copy-dependencies')
    os.system('gradle -x test build')
    # Check if ./releases directory existed, otherwise, create
    release_dir: str = os.path.join(project_dir, 'release')
    if not os.path.isdir(release_dir):
        os.mkdir(release_dir)
    # Move build artifact to release directory.
    # move source.zip and source.tar.gz to release directory
    build_zip:str = os.path.join(project_dir,'build/distributions/{}.zip'.format(artifact_base_name))
    release_zip:str = os.path.join(release_dir,artifact_base_name)
    release_zip = os.path.join(release_dir,'{}-{}.zip'.format(artifact_base_name, release_version))
    os.system('mv {} {}'.format(build_zip, release_zip))
    release_source_zip:str = os.path.join(release_dir, 'source-{}.zip'.format(release_version))
    release_source_targz: str = os.path.join(release_dir, 'source-{}.tar.gz'.format(release_version))
    logger.debug('Release source zip:{}'.format(release_source_zip))
    logger.debug('Release source tar.gz:{}'.format(release_source_targz))
    os.system('mv /tmp/source.zip {}'.format(release_source_zip))
    os.system('mv /tmp/source.tar.gz {}'.format(release_source_targz))

    # create version.txt
    logger.debug('Opening and writing version.txt')
    f = open(os.path.join(release_dir,'version.txt'), "w")
    f.write(release_version)
    f.close()

    # Modify the pom file and commit/push
    pom_modify_version:str = 'mvn versions:set -DnewVersion={} versions:commit'.format(release_version)
    os.system(pom_modify_version)
    # os.system(XXXXXXXXXX) //TODOs

    # Clean up target directory
    os.system('chmod -R 777 {}'.format(release_dir))
    os.system('chmod -R 777 {}'.format(os.path.join(project_dir, 'builder')))
    os.system('rm -Rf target')
    os.system('rm -Rf build')


    # to set new version :  mvn versions:set -DnewVersion=1.3.5 versions:commit
    # above command using versions:commit to remove .versionBackup file


if __name__ == '__main__':
    builder_obj:object = builder()
    process()
