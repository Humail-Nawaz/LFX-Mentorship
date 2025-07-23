from os.path import dirname, abspath, join, isdir
from shutil import rmtree
from random import seed, choice, randint
from os import environ
from subprocess import run

ROOT = dirname(
    abspath(__file__)
)
TARGET_DIR = join(ROOT, 'out', 'stack.SVGen')

if __name__ == '__main__':
    seed(32)
    if isdir(TARGET_DIR):
        rmtree(TARGET_DIR)
    for _ in range(10):
        data_width = choice((8, 16, 32))
        length = randint(1, 1024)
        environ['DATA_WIDTH'] = f'{data_width}'
        environ['LENGTH'] = f'{length}'
        exec_sp = run(
            f'sbt "runMain stack.SVGen {data_width} {length}"',
            shell = True,
            text = True
        )
        exec_sp.check_returncode()
        if exec_sp.returncode != 0:
            exit(1)
        exec_sp = run(
            'make',
            shell = True,
            text = True
        )
        exec_sp.check_returncode()
        if exec_sp.returncode != 0:
            exit(1)
